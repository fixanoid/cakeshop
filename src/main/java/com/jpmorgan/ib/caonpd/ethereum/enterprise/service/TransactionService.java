package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import java.util.List;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;

public interface TransactionService {

	public Transaction get(String address) throws APIException;

	public List<Transaction> list(String blockHash, Integer blockNumber) throws APIException;

	public List<Transaction> pending() throws APIException;

}
