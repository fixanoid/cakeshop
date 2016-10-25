package com.jpmorgan.ib.caonpd.cakeshop.db;

import com.google.common.collect.Lists;
import com.jpmorgan.ib.caonpd.cakeshop.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.cakeshop.dao.BlockDAO;
import com.jpmorgan.ib.caonpd.cakeshop.dao.TransactionDAO;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIResponse;
import com.jpmorgan.ib.caonpd.cakeshop.model.Block;
import com.jpmorgan.ib.caonpd.cakeshop.model.Transaction;
import com.jpmorgan.ib.caonpd.cakeshop.service.TransactionService;
import com.jpmorgan.ib.caonpd.cakeshop.service.WebSocketPushService;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SavingBlockListener implements BlockListener {

    private class BlockSaverThread extends Thread {
        public boolean running = true;

        public BlockSaverThread() {
            setName("BlockSaver-" + getId());
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Block block = blockQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (block != null) {
                        saveBlock(block);
                    }
                } catch (InterruptedException e) {
                    return;
                } catch (Throwable ex) {
                    LOG.error("BlockSaverThread died", ex);
                }
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SavingBlockListener.class);

    @Autowired
    private BlockDAO blockDAO;

    @Autowired
    private TransactionDAO txDAO;

    @Autowired
    private TransactionService txService;

    @Autowired
    private GethConfigBean gethConfig;

    private final ArrayBlockingQueue<Block> blockQueue;

    private final BlockSaverThread blockSaver;

    private static final String TOPIC = WebSocketPushService.TRANSACTION_TOPIC + "all";
    private static final String TOPIC_BLOCK = WebSocketPushService.BLOCK_TOPIC + "/all";
    

    @Autowired(required = false)
    private SimpMessagingTemplate stompTemplate;

    public SavingBlockListener() {
        blockQueue = new ArrayBlockingQueue<>(1000);
        blockSaver = new BlockSaverThread();              
    }
    
    @PostConstruct
    protected void init() {
        blockSaver.start();
    }

    @PreDestroy
    @Override
    public void shutdown() {
        LOG.info("shutdown");
        blockSaver.running = false;
        blockSaver.interrupt();
    }

    protected void saveBlock(Block block) {
        if (!gethConfig.isDbEnabled()) {
            return;
        }

        LOG.debug("Persisting block #" + block.getNumber());
        blockDAO.save(block);
        if (!block.getTransactions().isEmpty()) {
            List<String> transactions = block.getTransactions();
            List<List<String>> txnChunks = Lists.partition(transactions, 256);
            for (List<String> txnChunk : txnChunks) {
                try {
                    List<Transaction> txns = txService.get(txnChunk);
                    txDAO.save(txns);
                } catch (APIException e) {
                    LOG.warn("Failed to load transaction details for tx", e);
                }
            }
            pushBlockNumber(block.getNumber().longValue()); // push to subscribers after saving
        }
    }
    
    private void pushBlockNumber(Long blockNumber) {
        if (stompTemplate == null) {
            return;
        }
        LOG.debug("Pushing block number " + blockNumber);
        APIResponse res = new APIResponse();
        res.setData(new APIData(blockNumber.toString(), "block_number", blockNumber));
        stompTemplate.convertAndSend(TOPIC_BLOCK, res);
    }

    private void pushTransactions(List<Transaction> txns) {
        if (stompTemplate == null) {
            return;
        }
        LOG.debug("Transaction list size " + txns.size());
        for (Transaction txn : txns) {
            
            APIResponse res = new APIResponse();
            res.setData(txn.toAPIData());
            stompTemplate.convertAndSend(TOPIC, res);
        }
    }

    @Override
    public void blockCreated(Block block) {
        if (!gethConfig.isDbEnabled()) {
            return;
        }
        try {
            blockQueue.put(block);
        } catch (InterruptedException e) {
            return;
        }
    }

}
