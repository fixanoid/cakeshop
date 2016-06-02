package com.jpmorgan.ib.caonpd.cakeshop.client.ws;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.reflect.TypeToken;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.Node;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIResponse;

import java.io.IOException;

import org.springframework.messaging.simp.stomp.StompHeaders;

public abstract class NodeStatusEventHandler extends EventHandler {

    protected final TypeToken<APIResponse<APIData<Node>, Node>> typeToken = new TypeToken<APIResponse<APIData<Node>,Node>>(){};
    protected final JavaType valType = objectMapper.constructType(typeToken.getType());

    public static final String TOPIC = "/topic/node/status";

    public abstract void onData(Node node);

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        try {
            APIResponse<APIData<Node>, Node> val = objectMapper.readValue((String) payload, valType);
            onData(val.getData());

        } catch (IOException e) {
            throw new RuntimeException("Failed to decode message", e);
        }
    }


}
