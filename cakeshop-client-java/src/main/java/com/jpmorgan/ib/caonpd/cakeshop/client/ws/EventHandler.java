package com.jpmorgan.ib.caonpd.cakeshop.client.ws;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Type;

import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;

public abstract class EventHandler implements StompFrameHandler {

    protected static final ObjectMapper objectMapper = new ObjectMapper();

    public EventHandler() {
    }

    public abstract String getTopic();

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return String.class;
    }

//    private void initDataType() {
//        Method[] methods = getClass().getDeclaredMethods();
//        for (Method method : methods) {
//            if (method.getName().contentEquals("onData")) {
//                Type[] types = method.getGenericParameterTypes();
//                this.dataType = (Class<T>) types[0];
//                return;
//            }
//        }
//    }
//
//    public Class<T> getDataType() {
//        if (this.dataType == null) {
//            initDataType();
//        }
//        return this.dataType;
//    }

}
