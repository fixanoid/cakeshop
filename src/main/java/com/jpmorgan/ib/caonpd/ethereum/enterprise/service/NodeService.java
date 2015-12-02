package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Node;

public interface NodeService {
    
    public String NODE_RUNNING_STATUS="running";
    public String NODE_NOT_RUNNING_STATUS="stopped";

    public Node get();

}
