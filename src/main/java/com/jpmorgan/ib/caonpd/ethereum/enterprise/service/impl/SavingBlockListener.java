package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.google.common.collect.Lists;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.dao.BlockDAO;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.dao.TransactionDAO;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class SavingBlockListener implements BlockListener {

    private class BlockSaverThread extends Thread {
        public boolean running = true;

        public BlockSaverThread() {
            setName("BlockSaver");
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

    private ArrayBlockingQueue<Block> blockQueue;

    private final BlockSaverThread blockSaver;

    public SavingBlockListener() {
        blockQueue = new ArrayBlockingQueue<>(1000);
        blockSaver = new BlockSaverThread();
        blockSaver.start();
    }

    @PreDestroy
    public void shutdown() {
        blockSaver.running = false;
    }

    private void saveBlock(Block block) {
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
                    for (Transaction txn : txns) {
                        if (txn != null) {
                            txDAO.save(txn);
                        }
                    }
                } catch (APIException e) {
                    LOG.warn("Failed to load transaction details for tx", e);
                }
            }
        }
    }

    @Override
    public void blockCreated(Block block) {
        try {
            blockQueue.put(block);
        } catch (InterruptedException e) {
            return;
        }
    }

}
