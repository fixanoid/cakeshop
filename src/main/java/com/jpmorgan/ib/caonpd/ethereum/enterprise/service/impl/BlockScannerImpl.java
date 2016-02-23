package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.dao.BlockDAO;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.dao.TransactionDAO;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.BlockScanner;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.BlockService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
@Profile("container")
public class BlockScannerImpl extends Thread implements BlockScanner {

    private static final Logger LOG = LoggerFactory.getLogger(BlockScannerImpl.class);

    @Autowired
    private BlockDAO blockDAO;

    @Autowired
    private TransactionDAO txDAO;

    @Autowired
    private BlockService blockService;

    @Autowired
    private TransactionService txService;

    @Autowired
    private ContractService contractService;

    private Block previousBlock;

    private boolean stopped;
    private Object wakeup;

    public BlockScannerImpl() {
        this.stopped = false;
        this.setName("BlockScanner");
    }

    @Override
    public void shutdown() {
        this.stopped = true;
        synchronized(wakeup) {
            this.wakeup.notify();
        }
        try {
            this.join();
        } catch (InterruptedException e) {
        }
    }

    /**
     * Lookup the latest block in our DB and fill in missing blocks from the chain
     */
    protected void backfillBlocks() {

        // get the max block at startup
        Block largestSavedBlock = blockDAO.getLatest();
        Block chainBlock = null;
        try {
            chainBlock = blockService.get(null, null, "latest");
        } catch (APIException e) {
            LOG.warn("Failed to read latest block: " + e.getMessage(), e);
            return;
        }

        if (largestSavedBlock == null) {
            fillBlockRange(0, chainBlock.getNumber());

        } else if (chainBlock.getNumber() > largestSavedBlock.getNumber()) {
            fillBlockRange(largestSavedBlock.getNumber() + 1, chainBlock.getNumber());

        } else if (chainBlock.equals(largestSavedBlock)) {
            previousBlock = chainBlock;
        }

    }

    private void fillBlockRange(long start, long end) {

        LOG.debug("Filling blocks from " + start + " to " + end);

        for (long i = start; i <= end; i++) {
            try {
                LOG.debug("Filling block #" + i);
                Block block = blockService.get(null, i, null);
                saveBlock(block);
                previousBlock = block;

            } catch (APIException e) {
                LOG.warn("Failed to read block #" + i + ": " + e.getMessage(), e);
                return;
            }
        }

    }

    private void saveBlock(Block block) {
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
    public void run() {

        this.wakeup = new Object();

        backfillBlocks();

        while (true) {

            if (this.stopped) {
                LOG.debug("Stopping block scanner " + getName());
                return;
            }

            try {
                Block latestBlock = blockService.get(null, null, "latest");

                if (previousBlock == null || !previousBlock.equals(latestBlock)) {
                    if (previousBlock != null && (latestBlock.getNumber() - previousBlock.getNumber()) > 1) {
                        // block was just polled is ahead of what we have in our DB
                        fillBlockRange(previousBlock.getNumber() + 1, latestBlock.getNumber() - 1);
                    }

                    LOG.debug("Saving new block #" + latestBlock.getNumber());
                    saveBlock(latestBlock);
                    previousBlock = latestBlock;
                }

            } catch (APIException e1) {
                LOG.warn("Error fetching block: " + e1.getMessage());
            }

            synchronized(this.wakeup) {
                try {
                    TimeUnit.SECONDS.timedWait(this.wakeup, 2);
                } catch (InterruptedException e) {
                    return;
                }
            }

        }
    }

}
