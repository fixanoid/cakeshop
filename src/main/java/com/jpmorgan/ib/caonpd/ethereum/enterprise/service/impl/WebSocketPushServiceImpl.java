/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIData;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIResponse;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Block;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Contract;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Node;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.BlockService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.ContractService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.NodeService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.WebSocketAsyncPushService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.WebSocketPushService;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 *
 * @author i629630
 */
@Component
public class WebSocketPushServiceImpl implements WebSocketPushService, ApplicationListener {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(WebSocketPushServiceImpl.class);
    private Integer openedSessions = 0;
    private final Map <String,Integer> transactionsMap = new LRUMap(500);

    @Autowired(required = false)
    private SimpMessagingTemplate template;

    @Autowired
    private ContractService contractService;

    @Autowired
    private NodeService nodeService;

    @Autowired
    private BlockService blockService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WebSocketAsyncPushService asyncPushService;

    @Override
//    @Scheduled(fixedDelay = 5000)
    public void pushContracts() throws APIException {

        if (openedSessions > 0) {
            List<Contract> contracts = contractService.list();
            APIResponse apiResponse = new APIResponse();
            APIData data = new APIData();
            data.setAttributes(contracts);
            apiResponse.setData(data);
            template.convertAndSend(CONTRACT_TOPIC, apiResponse);
            LOG.info("Sending websocket contract reponse");
        }
    }

    @Override
    @Scheduled(fixedDelay = 5000)
    public void pushNodeStatus() throws APIException {

        if (openedSessions > 0) {
            Node node = nodeService.get();
            APIResponse apiResponse = new APIResponse();
            apiResponse.setData(new APIData(node.getId(), "node", node));
            template.convertAndSend(NODE_TOPIC, apiResponse);
        }
    }

    @Override
    @Scheduled(fixedDelay = 5000)
    public void pushLatestBlocks() throws APIException {

        if (openedSessions > 0) {
            Block block = blockService.get(null, null, "latest");
            APIResponse apiResponse = new APIResponse();
            apiResponse.setData(block.toAPIData());
            template.convertAndSend(BLOCK_TOPIC, apiResponse);
            List <String> transactions = block.getTransactions();
            for (String transaction : transactions) {
                if (transactionsMap.containsKey(transaction)) {
                    asyncPushService.pushTransactionAsync(transaction, template, null);
                }
            }
        }
    }

    @Override
//    @Scheduled(fixedDelay = 5000)
    public void pushPendingTransactions() throws APIException {

        if (openedSessions > 0) {
            List<Transaction> transactions = transactionService.pending();
            APIResponse apiResponse = new APIResponse();
            APIData data = new APIData();
            data.setAttributes(transactions);
            apiResponse.setData(data);
            template.convertAndSend(PENDING_TRANSACTIONS_TOPIC, apiResponse);
        }
    }

    @Override
    @Scheduled(fixedDelay = 200)
    public void pushTransactions() throws APIException {
        if (openedSessions > 0) {
            for (String transactionAddress : transactionsMap.keySet()) {
                asyncPushService.pushTransactionAsync(transactionAddress, template, transactionsMap);
            }
        }
    }

    public Integer getOpenedConnections() {
        return openedSessions;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        StompHeaderAccessor accessor;
        if (event instanceof SessionConnectEvent) {
            openedSessions++;
        }

        if (event instanceof SessionSubscribeEvent) {
            SessionSubscribeEvent subscribeEvent = (SessionSubscribeEvent) event;
            accessor = StompHeaderAccessor.wrap(subscribeEvent.getMessage());
            if (StringUtils.isNotBlank(accessor.getDestination()) && accessor.getDestination().startsWith(TRANSACTION_TOPIC)) {
                String transactionKey = accessor.getDestination()
                        .substring(accessor.getDestination().lastIndexOf("/") + 1, accessor.getDestination().length());
                Integer subscribers;
                if (transactionsMap.containsKey(transactionKey)) {
                    subscribers = transactionsMap.get(transactionKey);
                    transactionsMap.put(transactionKey, subscribers++);
                } else {
                    transactionsMap.put(transactionKey, 1);
                }
            }
        }

        if (event instanceof SessionUnsubscribeEvent) {
            SessionUnsubscribeEvent unsubscribeEvent = (SessionUnsubscribeEvent) event;
            accessor = StompHeaderAccessor.wrap(unsubscribeEvent.getMessage());
            if (StringUtils.isNotBlank(accessor.getSubscriptionId()) && accessor.getSubscriptionId().startsWith(TRANSACTION_TOPIC)) {
                String transactionKey = accessor.getSubscriptionId()
                        .substring(accessor.getSubscriptionId().lastIndexOf("/") + 1, accessor.getSubscriptionId().length());
                Integer subscribers;
                if (transactionsMap.containsKey(transactionKey)) {
                    subscribers = transactionsMap.get(transactionKey);
                    subscribers--;
                    if (subscribers <= 0) {
                        transactionsMap.remove(transactionKey);
                    } else {
                        transactionsMap.put(transactionKey, subscribers);
                    }
                }
            }
        }

        if (event instanceof SessionDisconnectEvent && openedSessions > 0) {
            openedSessions--;
        } else if (event instanceof SessionDisconnectEvent && openedSessions <= 0
                && !transactionsMap.isEmpty()) {
            transactionsMap.clear();
        }
    }
}
