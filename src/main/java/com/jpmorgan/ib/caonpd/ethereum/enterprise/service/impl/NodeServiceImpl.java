package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.google.gson.Gson;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;


import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.NodeService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Node;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.RequestModel;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

@Service
public class NodeServiceImpl implements NodeService {
    

    @Autowired
    private GethHttpService gethService;

    @Override
    public Node get(){

        Object input = null;
        Node node = new Node();

        Map<String, Object> data=null;
        
        try {
            //check if node is available 
            data = gethService.executeGethCall(AdminBean.ADMIN_NODE_INFO, new Object[]{ input, true });
            node.setStatus(StringUtils.isEmpty((String)data.get("NodeID"))?NodeService.NODE_NOT_RUNNING_STATUS:NodeService.NODE_RUNNING_STATUS);
            node.setId((String)data.get("NodeID"));
            //check if mining
            data = gethService.executeGethCall(AdminBean.ADMIN_MINER_MINING, new Object[]{ input, true });
            Boolean mining = (Boolean)data.get("id");
            node.setMining(mining==null?false:mining);
            //peer count
            data = gethService.executeGethCall(AdminBean.ADMIN_NET_PEER_COUNT, new Object[]{ input, true });
            String peerCount = (String)data.get("id");
            node.setPeerCount(peerCount==null?0:Integer.decode(peerCount));
            //get last block number
            data = gethService.executeGethCall(AdminBean.ADMIN_ETH_BLOCK_NUMBER, new Object[]{ input, true });
            String blockNumber = (String)data.get("id");
            node.setLatestBlock(blockNumber==null?0:Integer.decode(blockNumber));
            //get pending transactions
            data = gethService.executeGethCall(AdminBean.ADMIN_TXPOOL_STATUS, new Object[]{ input, true });
            Integer pending = (Integer)data.get("pending");
            node.setPendingTxn(pending==null?0:pending);
            
        } catch (APIException ex) {
            node.setStatus(NodeService.NODE_NOT_RUNNING_STATUS);
            Logger.getLogger(NodeServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception ex){
            node.setStatus(NodeService.NODE_NOT_RUNNING_STATUS);
            Logger.getLogger(NodeServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return node;
    }
   
}
