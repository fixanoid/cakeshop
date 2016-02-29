package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface TransactionService {

    /**
     * Get transaction information for the given ID
     *
     * @param id
     * @return
     * @throws APIException
     */
	public Transaction get(String id) throws APIException;

	/**
	 * List transactions in the given block
	 *
	 * @param blockHash
	 * @param blockNumber
	 * @return
	 * @throws APIException
	 */
	public List<Transaction> list(String blockHash, Integer blockNumber) throws APIException;

	/**
	 * Fetch transactions in a pending state (not yet committed)
	 *
	 * @return
	 * @throws APIException
	 */
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
