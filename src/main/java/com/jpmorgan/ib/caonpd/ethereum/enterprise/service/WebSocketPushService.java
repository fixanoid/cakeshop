package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;

/**
 *
 * @author i629630
 */
public interface WebSocketPushService {

    public final String CONTRACT_TOPIC = "/topic/contract";
    public final String NODE_TOPIC = "/topic/node/status";
    public final String BLOCK_TOPIC = "/topic/block";
    public final String PENDING_TRANSACTIONS_TOPIC = "/topic/pending/transactions";
    public final String TRANSACTION_TOPIC = "/topic/transaction/";
    public final Integer MAX_ASYNC_POOL = 100;

    public void pushContracts() throws APIException;
    public void pushNodeStatus() throws APIException;
    public void pushLatestBlocks() throws APIException;
    public void pushPendingTransactions() throws APIException;
    public void pushTransactions() throws APIException;

}
