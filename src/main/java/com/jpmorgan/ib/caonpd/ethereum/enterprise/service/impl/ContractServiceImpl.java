package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

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
    public List<Contract> compile(String code, CodeType codeType) throws APIException {

        List<Contract> contracts = new ArrayList<>();
        long createdDate = System.currentTimeMillis() / 1000;

		Map<String, Object> res = null;
		if (codeType == CodeType.solidity) {
			res = geth.executeGethCall("eth_compileSolidity", new Object[]{ code });
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

            // get binary
            contract.setBinary((String) compiled.get("code"));

            // get abi
            Object abiObj = ((Map<String, Object>) compiled.get("info")).get("abiDefinition");
            ObjectMapper mapper = new ObjectMapper();
            try {
                contract.setABI(mapper.writeValueAsString(abiObj));
            } catch (JsonProcessingException e) {
                throw new APIException("Unable to read ABI", e);
            }

            contracts.add(contract);
        }

		return contracts;
    }

    @Override
	public TransactionResult create(String code, CodeType codeType) throws APIException {
	    List<Contract> contracts = compile(code, codeType);
	    Contract contract = contracts.get(0); // FIXME for now, assume we are only deploying a single contract

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
		Map<String, Object> contractRes = geth.executeGethCall("eth_getCode", new Object[]{ address, "latest" });

		Contract contract = new Contract();
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
