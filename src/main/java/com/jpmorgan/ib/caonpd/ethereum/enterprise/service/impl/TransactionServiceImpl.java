package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import static com.jpmorgan.ib.caonpd.ethereum.enterprise.util.RpcUtil.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction.Status;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

	@Autowired
	GethHttpService geth;

	@Override
	public Transaction get(String address) throws APIException {


		Map<String, Object> txData = geth.executeGethCall("eth_getTransactionByHash", new Object[]{ address });
		Map<String, Object> txReceiptData = geth.executeGethCall("eth_getTransactionReceipt", new Object[]{ address });

		if (txReceiptData != null) {
			txData.putAll(txReceiptData);
		}

		Transaction tx = new Transaction();

		tx.setAddress((String) txData.get("hash"));
		tx.setBlockHash((String) txData.get("blockHash"));
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

		if (tx.getBlockHash() == null) {
			tx.setStatus(Status.pending);
		} else {
			tx.setStatus(Status.committed);
		}

		return tx;
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
	public Transaction waitForTx(TransactionResult result) throws APIException, InterruptedException {
	    Transaction tx = null;
	    while (true) {
	        tx = this.get(result.getId());
	        if (tx.getStatus() == Status.committed) {
	            break;
	        }
	        TimeUnit.MILLISECONDS.sleep(50);
	    }
	    return tx;
	}

}
