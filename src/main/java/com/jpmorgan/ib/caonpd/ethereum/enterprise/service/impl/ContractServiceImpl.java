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

	@Override
	public TransactionResult create(String abi, String code, CodeType codeType) throws APIException {

		Map<String, Object> res = null;
		if (codeType == CodeType.solidity) {
			res = geth.executeGethCall("eth_compileSolidity", new Object[]{ code });
		}

		String binaryCode = (String) res.get("code");

		Map<String, Object> contractArgs = new HashMap<String, Object>();
		contractArgs.put("from", "0x2e219248f44546d966808cdd20cb6c36df6efa82"); // FIXME remove this hardcoded and first getAccounts
		contractArgs.put("data", binaryCode);

		Map<String, Object> contractRes = geth.executeGethCall("eth_sendTransaction", new Object[]{ contractArgs });

		TransactionResult tr = new TransactionResult();
		tr.setId((String) contractRes.get("id"));

		return tr;
	}

	@Override
	public TransactionResult delete() throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Contract get() throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Contract> list() throws APIException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TransactionResult migrate() throws APIException {
		// TODO Auto-generated method stub
		return null;
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
