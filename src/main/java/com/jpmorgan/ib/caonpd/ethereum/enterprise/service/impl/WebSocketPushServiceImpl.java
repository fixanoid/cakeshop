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
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.WebSocketPushService;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 *
 * @author i629630
 */
@Component
public class WebSocketPushServiceImpl implements WebSocketPushService, ApplicationListener {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(WebSocketPushServiceImpl.class);

    private Integer openedSessions = 0;

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
        }
    }

    @Override
    @Scheduled(fixedDelay = 5000)
    public void pushTransactions() throws APIException {
        if (openedSessions > 0) {
            List<Transaction> transactions = transactionService.pending();
            APIResponse apiResponse = new APIResponse();
            APIData data = new APIData();
            data.setAttributes(transactions);
            apiResponse.setData(data);
            template.convertAndSend(PENDING_TRANSACTIONS_TOPIC, apiResponse);
        }
    }

    public Integer getOpenedConnections() {
        return openedSessions;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof SessionConnectEvent) {
            openedSessions++;
        }
        if (event instanceof SessionDisconnectEvent && openedSessions > 0) {
            openedSessions--;
        }
    }

    private void sendToTopic(String topic, APIResponse apiResponse) {
        template.convertAndSend(topic, apiResponse);
    }
}
