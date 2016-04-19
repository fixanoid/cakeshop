package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import java.util.Map;
import org.springframework.messaging.simp.SimpMessagingTemplate;

/**
 *
 * @author i629630
 */
public interface WebSocketAsyncPushService {

    public void pushTransactionAsync(String transactionAddress, SimpMessagingTemplate template, Map <String, Integer> transactions);

}
