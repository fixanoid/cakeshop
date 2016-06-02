package com.jpmorgan.ib.caonpd.cakeshop.client.ws;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.reflect.TypeToken;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.Transaction;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIResponse;

import java.io.IOException;

import org.springframework.messaging.simp.stomp.StompHeaders;

public abstract class TransactionEventHandler extends EventHandler {

    protected final TypeToken<APIResponse<APIData<Transaction>, Transaction>> typeToken =
            new TypeToken<APIResponse<APIData<Transaction>, Transaction>>(){};

    protected final JavaType valType = objectMapper.constructType(typeToken.getType());

    public static final String TOPIC = "/topic/transaction/";
    public static final String TOPIC_ALL = "/topic/transaction/all";

    public abstract void onData(Transaction txn);

    private final String txId;

    public TransactionEventHandler() {
        this(null);
    }

    public TransactionEventHandler(String txId) {
        this.txId = txId;
    }

    @Override
    public String getTopic() {
        if (txId != null) {
            return TOPIC + txId;
        } else {
            return TOPIC_ALL;
        }
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        try {
            APIResponse<APIData<Transaction>, Transaction> val = objectMapper.readValue((String) payload, valType);
            onData(val.getData());

        } catch (IOException e) {
            throw new RuntimeException("Failed to decode message", e);
        }
    }


}
