package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.ContractABI;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionRequest;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractRegistryService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.ProcessUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
public class ContractServiceImpl implements ContractService {

    private class ContractRegistrationTask implements Runnable {

        private final TransactionResult transactionResult;
        private final Contract contract;

        public ContractRegistrationTask(Contract contract, TransactionResult transactionResult) {
            this.contract = contract;
            this.transactionResult = transactionResult;
        }

        @Override
        public void run() {

            Transaction tx = null;

            while (tx == null) {
                try {
                    tx = transactionService.waitForTx(
                            transactionResult, pollDelayMillis, TimeUnit.MILLISECONDS);

                } catch (APIException e) {
                    LOG.warn("Error while waiting for contract to mine", e);
                    // TODO add backoff delay if server is down?
                } catch (InterruptedException e) {
                    LOG.warn("Interrupted while waiting for contract to mine", e);
                    return;
                }
            }

            try {
                contract.setAddress(tx.getContractAddress());
                contractRegistry.register(tx.getContractAddress(), contract.getName(), contract.getABI(),
                        contract.getCode(), contract.getCodeType(), contract.getCreatedDate());
            } catch (APIException e) {
                LOG.warn("Failed to register contract at address " + tx.getContractAddress(), e);
            }

        }
    }

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ContractServiceImpl.class);

    @Value("${contract.poll.delay.millis}")
    Long pollDelayMillis;

    // FIXME remove hardcoded FROM address
    public static final String DEFAULT_FROM_ADDRESS = "0x2e219248f44546d966808cdd20cb6c36df6efa82";

    @Autowired
    private GethConfigBean gethConfig;

	@Autowired
	private GethHttpService geth;

	@Autowired
	private ContractRegistryService contractRegistry;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	@Qualifier("asyncExecutor")
	private TaskExecutor executor;

    @Override
	@SuppressWarnings("unchecked")
    public List<Contract> compile(String code, CodeType codeType, Boolean optimize) throws APIException {

        if (codeType != CodeType.solidity) {
            throw new APIException("Only 'solidity' source is currently supported");
        }

        if (optimize == null) {
            optimize = true; // default to true
        }

        List<Contract> contracts = new ArrayList<>();
        long createdDate = System.currentTimeMillis() / 1000;

        Map<String, Object> res = null;
        try {
            String solc = gethConfig.getSolcPath();
            if (SystemUtils.IS_OS_WINDOWS) {
                solc = solc + ".cmd";
            }
            List<String> args = Lists.newArrayList(solc, "--ipc");

            ProcessBuilder builder = ProcessUtils.createProcessBuilder(gethConfig, args);

            //RpcUtil.puts(builder);
            Process proc = builder.start();

            StreamGobbler stdout = StreamGobbler.create(proc.getInputStream());
            StreamGobbler stderr = StreamGobbler.create(proc.getErrorStream());

            proc.getOutputStream().write(code.getBytes());;
            proc.getOutputStream().close();

            proc.waitFor();

            if (proc.exitValue() != 0) {
                throw new APIException("Failed to compile contract (solc exited with code " + proc.exitValue() + ")\n" + stderr.getString());
            }

            //System.out.println(stdout.getString());

            ObjectMapper mapper = new ObjectMapper();
            res = mapper.readValue(stdout.getString(), Map.class);

        } catch (IOException | InterruptedException e) {
            throw new APIException("Failed to compile contract", e);
        }

		// res is a hash of contract name -> compiled result map
		for (Entry<String, Object> contractRes : res.entrySet()) {
            Contract contract = new Contract();
            contract.setName(contractRes.getKey());
            contract.setCreatedDate(createdDate);
            contract.setCode(code);
            contract.setCodeType(codeType);

            Map<String, Object> compiled = (Map<String, Object>) contractRes.getValue();
            //System.out.println(compiled);

            contract.setBinary((String) compiled.get("bin"));
            contract.setABI((String) compiled.get("abi"));
            contract.setGasEstimates((Map<String, Object>) compiled.get("gas"));
            contract.setFunctionHashes((Map<String, String>) compiled.get("hashes"));
            contract.setSolidityInterface((String) compiled.get("interface"));

            contracts.add(contract);
        }

		return contracts;
    }

    @Override
    public TransactionResult create(String code, CodeType codeType, Object[] args, String binary) throws APIException {

        List<Contract> contracts = compile(code, codeType, true); // always deploy optimized contracts

        Contract contract = null;
        if (binary != null && binary.length() > 0) {
            // look for binary in compiled output, comparing first 64 chars of binary
            String bin = binary.trim().substring(0, 63);
            for (Contract c : contracts) {
                if (c.getBinary().trim().startsWith(bin)) {
                    contract = c;
                    break;
                }
            }

            if (contract == null) {
                throw new APIException("Binary does not match given code");
            }

        } else {
            contract = contracts.get(0);

        }


		Map<String, Object> contractArgs = new HashMap<String, Object>();
		contractArgs.put("from", DEFAULT_FROM_ADDRESS);
		contractArgs.put("data", contract.getBinary());
		contractArgs.put("gas", 3_141_592);

		Map<String, Object> contractRes = geth.executeGethCall("eth_sendTransaction", new Object[]{ contractArgs });

		TransactionResult tr = new TransactionResult();
		tr.setId((String) contractRes.get("_result"));

		executor.execute(new ContractRegistrationTask(contract, tr));

		return tr;
	}

	@Override
	public TransactionResult delete() throws APIException {
		throw new APIException("Not yet implemented"); // TODO
	}

	@Override
	public Contract get(String address) throws APIException {
		Contract contract = contractRegistry.getById(address);

		Map<String, Object> contractRes = geth.executeGethCall("eth_getCode", new Object[]{ address, "latest" });
		contract.setBinary((String) contractRes.get("_result"));

		return contract;
	}

	@Override
	public List<Contract> list() throws APIException {
	    return contractRegistry.list();
	}

	@Override
	public TransactionResult migrate() throws APIException {
		throw new APIException("Not yet implemented"); // TODO
	}

	@Override
	public Object read(String id, String method, Object[] args) throws APIException {
	    return read(id, lookupABI(id), method, args);
	}

	@Override
	public Object read(String id, ContractABI abi, String method, Object[] args) throws APIException {
	    TransactionRequest req = new TransactionRequest(DEFAULT_FROM_ADDRESS, id, abi, method, args);

	    Map<String, Object> readRes = geth.executeGethCall("eth_call", req.getArgsArray());
	    String res = (String) readRes.get("_result");
	    Object[] decodedResults = req.getFunction().decodeHexResult(res);

	    if (req.getFunction().outputs.length == 1 && decodedResults.length == 1) {
	        return decodedResults[0];
	    }

	    return decodedResults;
	}

	@Override
	public TransactionResult transact(String id, String method, Object[] args) throws APIException {
	    return transact(id, lookupABI(id), method, args);
	}

	@Override
	public TransactionResult transact(String id, ContractABI abi, String method, Object[] args) throws APIException {
	    TransactionRequest req = new TransactionRequest(DEFAULT_FROM_ADDRESS, id, abi, method, args);

	    Map<String, Object> readRes = geth.executeGethCall("eth_sendTransaction", req.getArgsArray());
	    return new TransactionResult((String) readRes.get("_result"));
	}

	private ContractABI lookupABI(String id) throws APIException {

	    Contract contract = contractRegistry.getById(id);
	    if (contract == null) {
	        throw new APIException("Contract not in registry " + id);
	    }

	    try {
	        return new ContractABI(contract.getABI());
	    } catch (IOException e) {
	        throw new APIException("Invalid ABI", e);
	    }
	}

}
