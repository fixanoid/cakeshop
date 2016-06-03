package com.jpmorgan.ib.caonpd.cakeshop.client.ws;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.reflect.TypeToken;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.Node;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIData;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.res.APIResponse;

public abstract class NodeStatusEventHandler extends EventHandler<Node> {

    @SuppressWarnings("serial")
    private static final TypeToken<APIResponse<APIData<Node>, Node>> typeToken =
            new TypeToken<APIResponse<APIData<Node>, Node>>() {};

    private static final JavaType valType = objectMapper.constructType(typeToken.getType());

    public static final String TOPIC = "/topic/node/status";

    @Override
    public String getTopic() {
        return TOPIC;
    }

    @Override
    public JavaType getValType() {
        return valType;
    }

}
