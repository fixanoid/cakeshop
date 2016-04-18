package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.dao.BlockDAO;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.dao.TransactionDAO;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

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
        if (true) {
            return;
        }
        LOG.debug("Persisting block #" + block.getNumber());
        blockDAO.save(block);
        if (!block.getTransactions().isEmpty()) {
            for (String txId : block.getTransactions()) {
                try {
                    Transaction tx = txService.get(txId);
                    txDAO.save(tx);
                } catch (APIException e) {
                    LOG.warn("Failed to load transaction details for tx " + txId, e);
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
