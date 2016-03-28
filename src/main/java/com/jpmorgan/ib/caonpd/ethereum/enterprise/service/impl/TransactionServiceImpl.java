package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import static com.jpmorgan.ib.caonpd.ethereum.enterprise.util.RpcUtil.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.ContractABI;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction.Status;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.TransactionResult;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.io.IOException;
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
	GethHttpService geth;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public Transaction get(String id) throws APIException {


		Map<String, Object> txData = geth.executeGethCall("eth_getTransactionByHash", new Object[]{ id });
		if (txData == null) {
		    return null;
		}

		Map<String, Object> txReceiptData = geth.executeGethCall("eth_getTransactionReceipt", new Object[]{ id });

		if (txReceiptData != null) {
			txData.putAll(txReceiptData);
		}

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
		    Contract contract;
		    ContractABI abi;
		    try {
		        contract = contractService.get(tx.getTo());
		        if (contract != null && contract.getABI() != null && !contract.getABI().isEmpty()) {
		            abi = new ContractABI(contract.getABI());

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

		    } catch (IOException e) {
		        LOG.warn("Failed to load underlying contract: " + e.getMessage());
		    }

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
