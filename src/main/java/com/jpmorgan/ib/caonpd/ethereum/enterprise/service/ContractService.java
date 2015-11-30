package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import java.util.List;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;

public interface ContractService {

	/**
	 * List of allowable Code Types durinug create
	 * @author chetan
	 *
	 */
	public static enum CodeType {
		solidity,
		binary,
		llvm,
		serpent
	}

	public TransactionResult create(String abi, String code, CodeType codeType) throws APIException;

	public TransactionResult delete() throws APIException;

	public Contract get() throws APIException;

	public List<Contract> list() throws APIException;

	public TransactionResult migrate() throws APIException;

	public Object read() throws APIException;

	public TransactionResult transact() throws APIException;

}
