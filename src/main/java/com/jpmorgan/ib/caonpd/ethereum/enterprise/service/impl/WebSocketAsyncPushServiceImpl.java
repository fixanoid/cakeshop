/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIError;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIResponse;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Transaction;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.TransactionService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.WebSocketAsyncPushService;
import static com.jpmorgan.ib.caonpd.ethereum.enterprise.service.WebSocketPushService.TRANSACTION_TOPIC;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 *
 * @author i629630
 */
@Component
public class WebSocketAsyncPushServiceImpl implements WebSocketAsyncPushService {
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(WebSocketAsyncPushServiceImpl.class);
    
    @Autowired
    private TransactionService transactionService;

    @Override
    @Async
    public void pushTransactionAsync(final String transactionAddress, final SimpMessagingTemplate template, Map<String,Integer> transactions) {
        try {
            Transaction transaction = transactionService.get(transactionAddress);
            APIResponse apiResponse = new APIResponse();

            if (StringUtils.isNotBlank(transaction.getAddress())) {
                apiResponse.setData(transaction.toAPIData());
            } else {
                APIError err = new APIError();
                err.setStatus("404");
                err.setTitle("Transaction not found");
                apiResponse.addError(err);
                if (null != transactions && !transactions.isEmpty()) {
                    transactions.remove(transactionAddress);
                }
            }
            template.convertAndSend(TRANSACTION_TOPIC + transactionAddress, apiResponse);
        } catch (APIException ex) {
            LOG.error(ex.getMessage());
        }
    }
    
}
