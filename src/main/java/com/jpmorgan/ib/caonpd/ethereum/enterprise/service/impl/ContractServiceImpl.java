package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;

@Service
public class ContractServiceImpl implements ContractService {

	@Autowired
	GethHttpService geth;

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
		contractArgs.put("from", "0x2e219248f44546d966808cdd20cb6c36df6efa82"); // FIXME remove this hardcoded and first getAccounts
		contractArgs.put("data", binaryCode);
		contractArgs.put("gas", 3_141_592);

		Map<String, Object> contractRes = geth.executeGethCall("eth_sendTransaction", new Object[]{ contractArgs });

		TransactionResult tr = new TransactionResult();
		tr.setId((String) contractRes.get("id"));

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
		contract.setBinary((String) contractRes.get("id"));

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
	public Object read() throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransactionResult transact() throws APIException {
		// TODO Auto-generated method stub
		return null;
	}


}
