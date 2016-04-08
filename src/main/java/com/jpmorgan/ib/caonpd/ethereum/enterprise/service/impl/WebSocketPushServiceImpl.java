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

	/**
	 * Transaction ID -> # of subscribers
	 */
	private final Map<String, Integer> transactionsMap = new LRUMap(500);

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

	// For tracking status changes
	private Node previousNodeStatus;

	// For tracking block changes
	private Block previousBlock;

	@Autowired
	private MetricsBlockListener metricsBlockListener;

	@Scheduled(fixedDelay = 1000)
	public void pushTxnPerSec() {
	    System.err.println("pushing metrics");
	    template.convertAndSend(
	            "/topic/metrics/txnPerSec",
	            APIResponse.newSimpleResponse(metricsBlockListener.getTxnPerSec()));
	}

	@Override
	// @Scheduled(fixedDelay = 5000)
	public void pushContracts() throws APIException {
		if (openedSessions <= 0) {
		    return;
		}

        List<Contract> contracts = contractService.list();
        APIResponse apiResponse = new APIResponse();
        APIData data = new APIData();
        data.setAttributes(contracts);
        apiResponse.setData(data);
        template.convertAndSend(CONTRACT_TOPIC, apiResponse);
	}

	@Override
	@Scheduled(fixedDelay = 5000)
	public void pushNodeStatus() throws APIException {
		if (openedSessions <= 0) {
		    return;
		}

        Node node = nodeService.get();

        if (previousNodeStatus != null && node.equals(previousNodeStatus)) {
            return; // status has not changed...
        }
        previousNodeStatus = node;

        APIResponse apiResponse = new APIResponse();
        apiResponse.setData(new APIData(node.getId(), "node", node));
        template.convertAndSend(NODE_TOPIC, apiResponse);
	}

	@Override
	@Scheduled(fixedDelay = 5000)
	public void pushLatestBlocks() throws APIException {
		if (openedSessions <= 0) {
		    return;
		}

        Block block = blockService.get(null, null, "latest");

        if (previousBlock != null && block.equals(previousBlock)) {
            return; // did not change
        }
        previousBlock = block;

        APIResponse apiResponse = new APIResponse();
        apiResponse.setData(block.toAPIData());
        template.convertAndSend(BLOCK_TOPIC, apiResponse); // push block info

        // Also try to push tx info if block contains one we are watching
        List<String> transactions = block.getTransactions();
        for (String transaction : transactions) {
            if (transactionsMap.containsKey(transaction)) {
                asyncPushService.pushTransactionAsync(transaction, template, null);
            }
        }
	}

	@Override
	//@Scheduled(fixedDelay = 5000)
	public void pushPendingTransactions() throws APIException {
		if (openedSessions <= 0) {
		    return;
		}

        List<Transaction> transactions = transactionService.pending();
        APIResponse apiResponse = new APIResponse();
        APIData data = new APIData();
        data.setAttributes(transactions);
        apiResponse.setData(data);
        template.convertAndSend(PENDING_TRANSACTIONS_TOPIC, apiResponse);
	}

	@Override
	@Scheduled(fixedDelay = 200)
	public void pushTransactions() throws APIException {
		if (openedSessions <= 0 || transactionsMap.isEmpty()) {
		    return;
		}

        for (String transactionAddress : transactionsMap.keySet()) {
            asyncPushService.pushTransactionAsync(transactionAddress, template, transactionsMap);
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
            if (openedSessions <= 0 && !transactionsMap.isEmpty()) {
                transactionsMap.clear();
            }
		}

		if (event instanceof SessionSubscribeEvent) {
			onSessionSubscribe(event);
		}

		if (event instanceof SessionUnsubscribeEvent) {
			onSessionUnsubscribe(event);
		}

	}

    private void onSessionUnsubscribe(ApplicationEvent event) {
        SessionUnsubscribeEvent unsubscribeEvent = (SessionUnsubscribeEvent) event;
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(unsubscribeEvent.getMessage());

        String dest = accessor.getSubscriptionId();
        if (StringUtils.isBlank(dest)) {
            return;
        }

        if (dest.startsWith(TRANSACTION_TOPIC)) {
        	String transactionKey = dest.substring(dest.lastIndexOf("/") + 1);
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

    private void onSessionSubscribe(ApplicationEvent event) {
        SessionSubscribeEvent subscribeEvent = (SessionSubscribeEvent) event;
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(subscribeEvent.getMessage());

        String dest = accessor.getDestination();
        if (StringUtils.isBlank(dest)) {
            return;
        }

        if (dest.startsWith(TRANSACTION_TOPIC)) {
        	String transactionKey = dest.substring(dest.lastIndexOf("/") + 1);

        	if (transactionsMap.containsKey(transactionKey)) {
        		Integer subscribers = transactionsMap.get(transactionKey);
        		transactionsMap.put(transactionKey, subscribers++);
        	} else {
        		transactionsMap.put(transactionKey, 1);
        	}
        }

        // send status as soon as there is a subscription
        if (dest.startsWith(NODE_TOPIC)) {
            try {
                previousNodeStatus = null; // force push
                pushNodeStatus();
            } catch (APIException e) {
            }
        }
    }
}
