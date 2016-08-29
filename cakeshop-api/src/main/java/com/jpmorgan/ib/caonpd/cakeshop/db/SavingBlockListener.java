package com.jpmorgan.ib.caonpd.cakeshop.db;

import com.google.common.collect.Lists;
import com.jpmorgan.ib.caonpd.cakeshop.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.LatestBlockNumber;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository.BlockRepository;
import com.jpmorgan.ib.caonpd.cakeshop.cassandra.repository.TransactionRepository;
import com.jpmorgan.ib.caonpd.cakeshop.dao.BlockDAO;
import com.jpmorgan.ib.caonpd.cakeshop.dao.TransactionDAO;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.APIResponse;
import com.jpmorgan.ib.caonpd.cakeshop.model.Block;
import com.jpmorgan.ib.caonpd.cakeshop.model.Transaction;
import com.jpmorgan.ib.caonpd.cakeshop.service.TransactionService;
import com.jpmorgan.ib.caonpd.cakeshop.service.WebSocketPushService;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;
import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class SavingBlockListener implements BlockListener {

        
    private final String EMPTY_ADDRESS = "0x";
    
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

    @Autowired(required = false)
    private BlockRepository blockRepository;
    @Autowired(required = false)
    private BlockDAO blockDAO;

    @Autowired(required = false)
    private TransactionRepository txRepository;
    @Autowired(required = false)
    private TransactionDAO txDAO;

    @Autowired
    private TransactionService txService;

    @Autowired
    private GethConfigBean gethConfig;

    private final ArrayBlockingQueue<Block> blockQueue;

    private final BlockSaverThread blockSaver;

    private static final String TOPIC = WebSocketPushService.TRANSACTION_TOPIC + "all";

    @Autowired(required = false)
    private SimpMessagingTemplate stompTemplate;

    public SavingBlockListener() {
        blockQueue = new ArrayBlockingQueue<>(1000);
        blockSaver = new BlockSaverThread();
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
        LOG.info("Persisting block #" + block.getNumber());
        if (null != blockRepository) {
            LatestBlockNumber latest = new LatestBlockNumber();
            latest.setBlockNumber(block.getNumber());
            blockRepository.save(latest);
            com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Block cassBlock = new com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Block();
            BeanUtils.copyProperties(block, cassBlock);
            blockRepository.save(cassBlock);
        } else if (null != blockDAO) {
            blockDAO.save(block);
        }
        
        if (!block.getTransactions().isEmpty()) {
            List<String> transactions = block.getTransactions();
            List<List<String>> txnChunks = Lists.partition(transactions, 256);
            for (List<String> txnChunk : txnChunks) {
                try {
                    List<Transaction> txns = txService.get(txnChunk);
                    if (null != txRepository) {
                        List<com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Transaction> cassTxns = new ArrayList();
                        for (Transaction txn : txns) {
                            com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Transaction cassTxn = new com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Transaction();
                            String[] excludedProperties = {"logs"};
                            BeanUtils.copyProperties(txn, cassTxn, excludedProperties);
                            if(StringUtils.isBlank(cassTxn.getContractAddress())){
                                cassTxn.setContractAddress(EMPTY_ADDRESS);
                            }
                            if(StringUtils.isBlank(cassTxn.getTo())){
                                cassTxn.setTo(EMPTY_ADDRESS);
                            }
                            cassTxns.add(cassTxn);
                        }
                        txRepository.save(cassTxns);                        
                    }
                    if (null != txDAO) {
                        txDAO.save(txns);
                    }
                    pushTransactions(txns); // push to subscribers after saving
                } catch (APIException e) {
                    LOG.warn("Failed to load transaction details for tx", e);
                }
            }
        }
    }

    private void pushTransactions(List<Transaction> txns) {
        if (stompTemplate == null) {
            return;
        }
        for (Transaction txn : txns) {
            APIResponse res = new APIResponse();
            res.setData(txn.toAPIData());
            stompTemplate.convertAndSend(TOPIC, res);
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
