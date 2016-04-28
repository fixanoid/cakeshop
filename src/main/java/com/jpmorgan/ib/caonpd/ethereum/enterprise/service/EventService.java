package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Event;

import java.util.List;
import java.util.Map;

public interface EventService {

    public List<Event> listForBlock(Long blockNumber) throws APIException;

    List<Event> processEvents(List<Map<String, Object>> rawEvents) throws APIException;

}
