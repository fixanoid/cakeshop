package com.jpmorgan.ib.caonpd.ethereum.enterprise.db;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.dao.BlockDAO;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.dao.TransactionDAO;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.BlockService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.NodeService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;


/**
 * Service which tracks the Blockchain in a local database. As blocks are added
 * to the chain, they are added to our database in near-realtime.
 *
 * @author chetan
 *
 */
@Service
@Scope("prototype")
@Profile("container")
public class BlockScanner extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(BlockScanner.class);

    @Autowired
    private BlockDAO blockDAO;

    @Autowired
    private TransactionDAO txDAO;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private TransactionService txService;

    @Autowired
    private GethConfigBean gethConfig;

    @Autowired
    private ApplicationContext appContext;

    private Collection<BlockListener> blockListeners;

    private Block previousBlock;

    private Integer previousPeerCount;

    private boolean stopped;
    private Object wakeup;

    public BlockScanner() {
        this.stopped = false;
        this.setName("BlockScanner");
    }

    @PostConstruct
    public void init() {
        this.blockListeners = appContext.getBeansOfType(BlockListener.class).values();
    }

    public void shutdown() {
        this.stopped = true;
        synchronized(wakeup) {
            this.wakeup.notify();
        }
        if (this.blockListeners != null && !this.blockListeners.isEmpty()) {
            for (BlockListener blockListener : blockListeners) {
                blockListener.shutdown();
            }

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

        long step = 99L;

        long i = 0;
        while (true) {
            i = start + step;
            if (i > end) {
                i = end;
            }
            try {
                List<Block> blocks = blockService.get(start, i);
                for (Block block : blocks) {
                    notifyListeners(block);
                    previousBlock = block;
                }

            } catch (APIException e) {
                LOG.warn("Failed to read blocks " + start + "-" + i, e);
            }
            if (i >= end) {
                break;
            }
            start = i+1;
        }
    }

    /**
     * Propagate block to all registered listeners
     * @param block
     */
    private void notifyListeners(Block block) {
        for (BlockListener blockListener : blockListeners) {
            blockListener.blockCreated(block);
        }
    }

    private void handleChainReorg() {

        // Search for ContractRegistry address in first two blocks
        boolean found = false;
        for (int i = 1; i < 10; i++) {
            try {
                Block block = blockService.get(null, Integer.valueOf(i).longValue(), null);
                if (block != null && block.getTransactions() != null && !block.getTransactions().isEmpty()) {
                    String txId = block.getTransactions().get(0);
                    Transaction tx = txService.get(txId);
                    if (tx != null && tx.getContractAddress() != null) {
                        // found it!
                        LOG.info("Found new ContractRegistry address " + tx.getContractAddress() + " at block #" + i);
                        saveContractRegistryAddress(tx.getContractAddress());
                        found = true;
                        break;
                    }
                }

            } catch (APIException e) {
                LOG.warn("Failed to read block", e);
            }
        }

        if (!found) {
            LOG.error("Couldn't find ContractRegistry address after chain reorg event");
            // TODO how to recover from this? Try again after a timeout?
        }

        // flush db
        LOG.info("Flushing DB");
//        dbConfig.reset();
        blockDAO.reset();
        txDAO.reset();

        // fill
        LOG.info("Backfilling blocks with new chain");
        backfillBlocks();
    }

    private void saveContractRegistryAddress(String addr) throws APIException {
        try {
            gethConfig.setProperty("contract.registry.addr", addr);
            gethConfig.save();
        } catch (IOException e) {
            LOG.warn("Unable to update env.properties", e);
            throw new APIException("Unable to update env.properties", e);
        }
    }

    private void checkDbSync() throws APIException {
        Block firstChainBlock = blockService.get(null, 1L, null);
        Block firstKnownBlock = blockDAO.getByNumber(1L);
        if ((firstKnownBlock != null && firstChainBlock != null && !firstKnownBlock.equals(firstChainBlock))
                || (firstChainBlock == null && firstKnownBlock != null)) {

            // if block #1 changed, we have a reorg
            LOG.warn("Detected chain reorganization do to new peer connection!");
            handleChainReorg();
            previousBlock = null;
        }
    }

    /**
     * Sleep for the given duration, returns false if interrupted
     *
     * @param seconds
     * @return
     */
    private boolean sleep(int seconds) {
        synchronized(this.wakeup) {
            try {
                TimeUnit.SECONDS.timedWait(this.wakeup, seconds);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void run() {

        this.wakeup = new Object();

        if (!sleep(5)) { // delayed startup
            return;
        }

        try {
            checkDbSync();
        } catch (APIException e) {
            LOG.warn("Failed to check sync status", e);
        }

        backfillBlocks();

        while (true) {

            if (this.stopped) {
                LOG.debug("Stopping block scanner " + getName());
                return;
            }

            try {
                Integer latestPeerCount = nodeService.peers().size();
                if (previousPeerCount != null && latestPeerCount > previousPeerCount) {
                    // peer count increased, try to detect chain re-org

                    // 10sec sleep while blocks sync up a bit
                    if (!sleep(10)) {
                        return;
                    }

                    checkDbSync();
                }

                Block latestBlock = blockService.get(null, null, "latest");
                if (previousBlock == null) {
                    previousBlock = blockDAO.getLatest();
                }

                if (previousBlock == null || !previousBlock.equals(latestBlock)) {
                    if (previousBlock != null && (latestBlock.getNumber() - previousBlock.getNumber()) > 1) {
                        // block that was just polled is ahead of what we have in our DB
                        fillBlockRange(previousBlock.getNumber() + 1, latestBlock.getNumber() - 1);
                    }

                    LOG.debug("Saving new block #" + latestBlock.getNumber());
                    notifyListeners(latestBlock);
                    previousBlock = latestBlock;
                }

                previousPeerCount = latestPeerCount;

            } catch (APIException e1) {
                LOG.warn("Error fetching block: " + e1.getMessage());
            }

            if (!sleep(2)) {
                return;
            }

        }
    }

}
