package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Node;
import java.util.Map;

public interface NodeService {
    
    public String NODE_RUNNING_STATUS="running";
    public String NODE_NOT_RUNNING_STATUS="stopped";

    public Node get() throws APIException;
    public void updateNodeInfo(Map <String, String> newProps) throws APIException;
    public Boolean resetNodeInfo();

}
