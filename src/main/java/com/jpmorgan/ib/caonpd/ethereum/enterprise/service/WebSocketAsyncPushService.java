/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
