package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIData;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Node;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.NodeInfo;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Peer;
import java.util.List;
import java.util.Map;

public interface NodeService {
    
    public String NODE_RUNNING_STATUS="running";
    public String NODE_NOT_RUNNING_STATUS="stopped";

    public Node get() throws APIException;
    public void updateNodeInfo(Map <String, String> newProps,Boolean requiresRestart) throws APIException;
    public NodeInfo update(Integer logLevel,Integer networkID,String identity,Boolean mining) throws APIException;
    public APIData getAPIData(Map data);
    public Boolean resetNodeInfo();
    public List<Peer> peers() throws APIException;

}
