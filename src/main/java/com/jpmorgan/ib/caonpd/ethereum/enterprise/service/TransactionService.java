package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface TransactionService {

	public Transaction get(String address) throws APIException;

	public List<Transaction> list(String blockHash, Integer blockNumber) throws APIException;

	public List<Transaction> pending() throws APIException;

	/**
	 * Wait for the given Transaction to be mined (blocks until completed)
	 *
	 * @param result
	 * @return {@link Transaction}
	 * @throws APIException
	 * @throws InterruptedException
	 */
    public Transaction waitForTx(TransactionResult result, long pollDelay, TimeUnit pollDelayUnit) throws APIException, InterruptedException;

}
