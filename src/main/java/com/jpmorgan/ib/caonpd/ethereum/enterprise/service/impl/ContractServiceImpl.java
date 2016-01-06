package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionRequest;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractRegistryService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContractServiceImpl implements ContractService {

    // FIXME remove hardcoded FROM address
    public static final String DEFAULT_FROM_ADDRESS = "0x2e219248f44546d966808cdd20cb6c36df6efa82";

	@Autowired
	GethHttpService geth;

	@Autowired
	ContractRegistryService contractRegistry;

	@SuppressWarnings("unchecked")
    @Override
	public TransactionResult create(String abi, String code, CodeType codeType) throws APIException {

		Map<String, Object> res = null;
		if (codeType == CodeType.solidity) {
			res = geth.executeGethCall("eth_compileSolidity", new Object[]{ code });
		}

		Map<String, Object> compiled  = (Map<String, Object>) res.values().toArray()[0];

		String binaryCode = (String) compiled.get("code");

		Map<String, Object> contractArgs = new HashMap<String, Object>();
		contractArgs.put("from", DEFAULT_FROM_ADDRESS);
		contractArgs.put("data", binaryCode);
		contractArgs.put("gas", 3_141_592);

		Map<String, Object> contractRes = geth.executeGethCall("eth_sendTransaction", new Object[]{ contractArgs });

		TransactionResult tr = new TransactionResult();
		tr.setId((String) contractRes.get("_result"));

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
		throw new APIException("Not yet implemented"); // TODO
	}

	@Override
	public TransactionResult migrate() throws APIException {
		throw new APIException("Not yet implemented"); // TODO
	}

	@Override
	public Object read(String id, String method, Object[] args) throws APIException {
	    Contract contract = contractRegistry.getById(id);
	    if (contract == null) {
	        throw new APIException("Contract not in registry " + id);
	    }
	    return read(id, contract.getABI(), method, args);
	}

	@Override
	public Object read(String id, String abi, String method, Object[] args) throws APIException {

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
	    Contract contract = contractRegistry.getById(id);
	    if (contract == null) {
	        throw new APIException("Contract not in registry " + id);
	    }
	    return transact(id, contract.getABI(), method, args);
	}

	@Override
	public TransactionResult transact(String id, String abi, String method, Object[] args) throws APIException {

	    TransactionRequest req = new TransactionRequest(DEFAULT_FROM_ADDRESS, id, abi, method, args);

	    Map<String, Object> readRes = geth.executeGethCall("eth_sendTransaction", req.getArgsArray());
	    return new TransactionResult((String) readRes.get("_result"));
	}

}
