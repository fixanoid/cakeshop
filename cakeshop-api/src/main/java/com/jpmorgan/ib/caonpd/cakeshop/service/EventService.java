package com.jpmorgan.ib.caonpd.cakeshop.service;

import com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity.Event;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import java.io.IOException;

import java.util.List;
import java.util.Map;

public interface EventService {

    public List<Event> listForBlock(Long blockNumber) throws APIException;

    List<Event> processEvents(List<Map<String, Object>> rawEvents) throws APIException;
    public String serialize(Object obj) throws IOException;
    public Object deserialize(String data) throws IOException, ClassNotFoundException;


}
