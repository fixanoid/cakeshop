package com.jpmorgan.ib.caonpd.cakeshop.service;

import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.Event;

import java.util.List;
import java.util.Map;

public interface EventService {

    public List<Event> listForBlock(Long blockNumber) throws APIException;

    List<Event> processEvents(List<Map<String, Object>> rawEvents) throws APIException;

}
