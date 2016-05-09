package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import static com.jpmorgan.ib.caonpd.ethereum.enterprise.util.RpcUtil.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.ContractABI;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.RequestModel;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction.Status;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.EventService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionServiceImpl.class);

	@Autowired
	private GethHttpService geth;

	@Autowired
	private EventService eventService;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public Transaction get(String id) throws APIException {

	    List<RequestModel> reqs = new ArrayList<>();
	    reqs.add(new RequestModel("eth_getTransactionByHash", new Object[]{ id }, 1L));
	    reqs.add(new RequestModel("eth_getTransactionReceipt", new Object[]{ id }, 2L));
	    List<Map<String, Object>> batchRes = geth.batchExecuteGethCall(reqs);

		if (batchRes.isEmpty() || batchRes.get(0) == null) {
		    return null;
		}

		Map<String, Object> txData = batchRes.get(0);
		if (batchRes.get(1) != null) {
		    txData.putAll(batchRes.get(1));
		}

		Transaction tx = processTx(txData);

		return tx;
	}

    private Transaction processTx(Map<String, Object> txData) throws APIException {
        Transaction tx = new Transaction();

		tx.setId((String) txData.get("hash"));
		tx.setBlockId((String) txData.get("blockHash"));
		tx.setContractAddress((String) txData.get("contractAddress"));
		tx.setNonce((String) txData.get("nonce"));
		tx.setInput((String) txData.get("input"));
		tx.setFrom((String) txData.get("from"));
		tx.setTo((String) txData.get("to"));
		tx.setGasPrice(toLong("gasPrice", txData));

		tx.setTransactionIndex(toLong("transactionIndex", txData));
		tx.setBlockNumber(toLong("blockNumber", txData));
		tx.setValue(toLong("value", txData));
		tx.setGas(toLong("gas", txData));
		tx.setCumulativeGasUsed(toLong("cumulativeGasUsed", txData));
		tx.setGasUsed(toLong("gasUsed", txData));

		if (tx.getBlockId() == null || tx.getBlockNumber() == null
		        || tx.getBlockId().contentEquals("0x0000000000000000000000000000000000000000000000000000000000000000")) {

			tx.setStatus(Status.pending);
		} else {
			tx.setStatus(Status.committed);
		}

		// Decode tx input
		if (tx.getContractAddress() == null && tx.getStatus() == Status.committed) {

		    // lookup contract
		    ContractService contractService = applicationContext.getBean(ContractService.class);
            Contract contract = contractService.get(tx.getTo());
            if (contract != null && contract.getABI() != null && !contract.getABI().isEmpty()) {
                ContractABI abi = ContractABI.fromJson(contract.getABI());

                String origInput = tx.getInput();
                if (origInput != null && origInput.startsWith("0xfa")) {
                    // handle gemini payloads
                    try {
                        Map<String, Object> res = geth.executeGethCall("eth_getGeminiPayload", new Object[] { tx.getInput() });
                        if (res.get("_result") != null) {
                            tx.setInput((String) res.get("_result"));
                        }
                    } catch (APIException e) {
                        LOG.warn("Failed to load gemini payload: " + e.getMessage());
                    }
                }

                tx.decodeInput(abi);
                tx.setInput(origInput); // restore original input after [gemini] decode
            }

		}


		if (txData.get("logs") != null) {
		    List<Map<String, Object>> logs = (List<Map<String, Object>>) txData.get("logs");
		    if (!logs.isEmpty()) {
		        tx.setLogs(eventService.processEvents(logs));
		    }
		}


        return tx;
    }

	@Override
	public List<Transaction> get(List<String> ids) throws APIException {

	    List<RequestModel> reqs = new ArrayList<>();
	    for (String id : ids) {
            reqs.add(new RequestModel("eth_getTransactionByHash", new Object[]{ id }, 1L));
            reqs.add(new RequestModel("eth_getTransactionReceipt", new Object[]{ id }, 2L));
        }
	    List<Map<String, Object>> batchRes = geth.batchExecuteGethCall(reqs);

	    // merge pairs of requests for all txns into single map
	    Map<String, Map<String, Object>> txnResponses = new HashMap<>();
	    for (Map<String, Object> res : batchRes) {
	        if (res != null) {
	            String hash = null;
	            if (res.get("hash") != null) {
	                hash = (String) res.get("hash");
	            } else if (res.get("transactionHash") != null) {
	                hash = (String) res.get("transactionHash");
	            }
	            if (hash != null) {
                    Map<String, Object> map = txnResponses.get(hash);
                    if (map != null) {
                        map.putAll(res); // add to existing map
                    } else {
                        txnResponses.put(hash, res); // insert new map
                    }
	            }
	        }
        }

	    // collect txns in the order they were requested
	    List<Transaction> txns = new ArrayList<>();
	    for (String id : ids) {
	        Map<String, Object> txData = txnResponses.get(id);
	        txns.add(processTx(txData));
        }

	    return txns;
	}

	@Override
	public List<Transaction> list(String blockHash, Integer blockNumber) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Transaction> pending() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Transaction waitForTx(TransactionResult result, long pollDelay, TimeUnit pollDelayUnit)
	        throws APIException, InterruptedException {

	    Transaction tx = null;
	    while (true) {
	        tx = this.get(result.getId());
	        if (tx.getStatus() == Status.committed) {
	            break;
	        }
	        pollDelayUnit.sleep(pollDelay);
	    }
	    return tx;
	}

}
