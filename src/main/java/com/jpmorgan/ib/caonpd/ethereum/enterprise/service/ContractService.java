package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;

import java.util.List;

public interface ContractService {

	/**
	 * List of allowable Code Types during contract create
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

	public Contract get(String address) throws APIException;

	public List<Contract> list() throws APIException;

	public TransactionResult migrate() throws APIException;

	public Object read(String id, String method, Object[] args) throws APIException;

	public Object read(String id, String abi, String method, Object[] args) throws APIException;

	public TransactionResult transact(String id, String method, Object[] args) throws APIException;

	public TransactionResult transact(String id, String abi, String method, Object[] args) throws APIException;

}
