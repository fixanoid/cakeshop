package com.jpmorgan.ib.caonpd.cakeshop.service.impl;

//import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Transaction;
//import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Event;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository.EventsRepository;
import static com.jpmorgan.ib.caonpd.cakeshop.util.AbiUtils.*;

import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.Contract;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI;
import com.jpmorgan.ib.caonpd.cakeshop.model.Event;
import com.jpmorgan.ib.caonpd.cakeshop.model.RequestModel;
import com.jpmorgan.ib.caonpd.cakeshop.model.Transaction;
import com.jpmorgan.ib.caonpd.cakeshop.model.TransactionResult;
import com.jpmorgan.ib.caonpd.cakeshop.model.Transaction.Status;
import com.jpmorgan.ib.caonpd.cakeshop.model.DirectTransactionRequest;
import com.jpmorgan.ib.caonpd.cakeshop.service.ContractService;
import com.jpmorgan.ib.caonpd.cakeshop.service.EventService;
import com.jpmorgan.ib.caonpd.cakeshop.service.GethHttpService;
import com.jpmorgan.ib.caonpd.cakeshop.service.TransactionService;
import com.jpmorgan.ib.caonpd.cakeshop.service.WalletService;
import java.io.IOException;
import java.math.BigInteger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
    private WalletService walletService;
   // @Autowired(required = false)
    private EventsRepository eventRepository;

    @Autowired
    private ApplicationContext applicationContext;

    private String defaultFromAddress;

    @Override
    public Transaction get(String id) throws APIException {
        List<RequestModel> reqs = new ArrayList<>();
        reqs.add(new RequestModel("eth_getTransactionByHash", new Object[]{id}, 1L));
        reqs.add(new RequestModel("eth_getTransactionReceipt", new Object[]{id}, 2L));
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
        //TODO: this is a hack to make test happy. Need to evaluate the logic to have to and contract address always present.
        tx.setContractAddress((String) txData.get("contractAddress"));
        tx.setTo((String)txData.get("to"));
        //hack end
        tx.setNonce((String) txData.get("nonce"));
        tx.setInput((String) txData.get("input"));
        tx.setFrom((String) txData.get("from"));

        tx.setGasPrice(toBigInt("gasPrice", txData));

        tx.setTransactionIndex(toBigInt("transactionIndex", txData));
        tx.setBlockNumber(toBigInt("blockNumber", txData));
        tx.setValue(toBigInt("blockNumber", txData));
        tx.setGas(toBigInt("gas", txData));
        tx.setCumulativeGasUsed(toBigInt("cumulativeGasUsed", txData));
        tx.setGasUsed(toBigInt("gasUsed", txData));

        if (tx.getBlockId() == null || tx.getBlockNumber() == null
                || tx.getBlockId().contentEquals("0x0000000000000000000000000000000000000000000000000000000000000000")) {

            tx.setStatus(Status.pending);
        } else {
            tx.setStatus(Status.committed);
        }

        if (tx.getContractAddress() == null && tx.getStatus() == Status.committed) {  

            // lookup contract
            ContractService contractService = applicationContext.getBean(ContractService.class);
            Contract contract = null;
            try {
                contract = contractService.get(tx.getTo());
            } catch (APIException e) {}
            String origInput = tx.getInput();
            if (contract != null && contract.getABI() != null && !contract.getABI().isEmpty()) {
                ContractABI abi = ContractABI.fromJson(contract.getABI());
                if (origInput != null && origInput.startsWith("0xfa")) {
                    // handle gemini payloads
                    try {
                        Map<String, Object> res = geth.executeGethCall("eth_getGeminiPayload", new Object[]{tx.getInput()});
                        if (res.get("_result") != null) {
                            tx.setInput((String) res.get("_result"));
                        }
                    } catch (APIException e) {
                        LOG.warn("Failed to load gemini payload: " + e.getMessage());
                    }
                }

                tx.decodeContractInput(abi);
                tx.setInput(origInput); // restore original input after [gemini] decode
            } else if (contract == null) {
                if (tx.getInput() != null && tx.getInput().startsWith("0xfa")) {
                    // handle gemini payloads
                    try {
                        Map<String, Object> res = geth.executeGethCall("eth_getGeminiPayload", new Object[]{tx.getInput()});
                        if (res.get("_result") != null) {
                            tx.setInput((String) res.get("_result"));
                        }
                    } catch (APIException e) {
                        LOG.warn("Failed to load gemini payload: " + e.getMessage());
                    }
                }
                tx.decodeDirectTxnInput(tx.getInput());
                tx.setInput(origInput); // restore original input after [gemini] decode
            }  
        } 

        if (txData.get("logs") != null) {
            List<Map<String, Object>> logs = (List<Map<String, Object>>) txData.get("logs");
            if (!logs.isEmpty()) {
                List<Event> events = eventService.processEvents(logs);
                tx.setLogs(events);
                if (null != eventRepository) {
                    List <com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Event> cassEvents = new ArrayList();
                    for (Event event : events) {
                        com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Event cassEvent 
                                = new com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Event();
                        BeanUtils.copyProperties(event, cassEvent, new String [] {"data"});
                        cassEvent.setData(decodeCassEvent(event.getData()));
                        cassEvents.add(cassEvent);
                        
                    }
                    eventRepository.save(cassEvents);
                }
            }
        }

        return tx;
    }

    @Override
    public List<Transaction> get(List<String> ids) throws APIException {

        List<RequestModel> reqs = new ArrayList<>();
        for (String id : ids) {
            reqs.add(new RequestModel("eth_getTransactionByHash", new Object[]{id}, 1L));
            reqs.add(new RequestModel("eth_getTransactionReceipt", new Object[]{id}, 2L));
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
            if (tx.getStatus() == null ? Status.committed.toString() == null : tx.getStatus().equals(Status.committed)) {
                break;
            }
            pollDelayUnit.sleep(pollDelay);
        }
        return tx;
    }

    @Override
    public TransactionResult directTransact(DirectTransactionRequest request) throws APIException {
        if (defaultFromAddress == null) {
            defaultFromAddress = walletService.list().get(0).getAddress();
        }
        request.setFromAddress(
                StringUtils.isNotBlank(request.getFromAddress())
                        ? request.getFromAddress()
                        : defaultFromAddress); // make sure we have a non-null from address
        Map<String, Object> readRes = geth.executeGethCall("eth_sendTransaction", request.toGethArgs());
        return new TransactionResult((String) readRes.get("_result"));
    }

    private List<String> decodeCassEvent(Object [] decodeHex) {

        List<String> dataList = new ArrayList<>();
        for (Object decodeOdj : decodeHex) {
            try {
                dataList.add(eventService.serialize(decodeOdj));
            } catch (IOException ex) {
                LOG.error(ex.getMessage());
            }
        }
        return dataList;
    }
}
