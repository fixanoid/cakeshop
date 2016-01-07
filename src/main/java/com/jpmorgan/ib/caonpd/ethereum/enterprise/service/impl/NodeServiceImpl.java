package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIData;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Node;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.NodeService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.EEUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

@Service
public class NodeServiceImpl implements NodeService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Autowired
    private GethHttpService gethService;


    @Override
    public Node get() throws APIException {

        Object input = null;
        Node node = new Node();

        Map<String, Object> data = null;

        try {
            //check if node is available
            data = gethService.executeGethCall(AdminBean.ADMIN_NODE_INFO, new Object[]{ input, true });

            node.setId((String)data.get("NodeID"));
            node.setStatus(StringUtils.isEmpty((String)data.get("NodeID")) ? NodeService.NODE_NOT_RUNNING_STATUS : NodeService.NODE_RUNNING_STATUS);
            node.setNodeName((String)data.get("Name"));
            String nodeURI = (String)data.get("NodeUrl");
            if(StringUtils.isNotEmpty(nodeURI)){
                try {
                    URI uri = new URI(nodeURI);
                    String host = uri.getHost();
                    //if host or IP aren't set, then populate with localhost IP
                    if(StringUtils.isEmpty(host) || "[::]".equals(host) ||  "0.0.0.0".equalsIgnoreCase(host)){
                        
                        try {
                            String ip = EEUtils.getLocalIP();
                            uri = new URI(uri.getScheme(), uri.getUserInfo(), ip, uri.getPort(), null, uri.getQuery(), null);
                            node.setNodeUrl(uri.toString());
                            node.setNodeIP(ip);
                        
                        } catch (APIException ex) {
                            LOG.error(ex.getMessage());
                            node.setNodeUrl(nodeURI);
                            node.setNodeIP(host);
                        }
                        
                    }else{
                        node.setNodeUrl(nodeURI);
                    }
                } catch (URISyntaxException ex) {
                    LOG.error(ex.getMessage());
                    throw new APIException(ex.getMessage());
                }
            }
            //check if mining
            data = gethService.executeGethCall(AdminBean.ADMIN_MINER_MINING, new Object[]{ input, true });
            Boolean mining = (Boolean)data.get(GethHttpServiceImpl.SIMPLE_RESULT);
            node.setMining(mining == null ? false : mining);
            //peer count
            data = gethService.executeGethCall(AdminBean.ADMIN_NET_PEER_COUNT, new Object[]{ input, true });
            String peerCount = (String)data.get(GethHttpServiceImpl.SIMPLE_RESULT);
            node.setPeerCount(peerCount == null ? 0 : Integer.decode(peerCount));
            //get last block number
            data = gethService.executeGethCall(AdminBean.ADMIN_ETH_BLOCK_NUMBER, new Object[]{ input, true });
            String blockNumber = (String)data.get(GethHttpServiceImpl.SIMPLE_RESULT);
            node.setLatestBlock(blockNumber == null ? 0 : Integer.decode(blockNumber));
            //get pending transactions
            data = gethService.executeGethCall(AdminBean.ADMIN_TXPOOL_STATUS, new Object[]{ input, true });
            Integer pending = (Integer)data.get("pending");
            node.setPendingTxn(pending == null ? 0 : pending);

        } catch (APIException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof ResourceAccessException) {
                node.setStatus(NodeService.NODE_NOT_RUNNING_STATUS);
                return node;
            }
            
            throw ex;

        } catch (NumberFormatException ex){

            node.setStatus(NodeService.NODE_NOT_RUNNING_STATUS);
            LOG.error(ex.getMessage());
            throw new APIException(ex.getMessage());

        }

        return node;
    }

    @Override
    public void updateNodeInfo(Map<String, String> newProps) throws APIException {

        String prpsPath = GethHttpService.ROOT + File.separator + ".." + File.separator + "env.properties";
        try {
            InputStream input = new FileInputStream(prpsPath);
            Properties props = new Properties();
            props.load(input);
            Boolean needUpdate = false;
            for (String key : newProps.keySet()) {
                if (props.containsKey(key) && !props.getProperty(key).equalsIgnoreCase(newProps.get(key))) {
                    props.setProperty(key, newProps.get(key));
                    needUpdate = true;
                }
            }
            if (needUpdate) {
                try (FileOutputStream out = new FileOutputStream(prpsPath)) {
                    props.store(out, null);
                }
                restart();
            }
        } catch (IOException ex) {

            LOG.error(ex.getMessage());
            throw new APIException(ex.getMessage());

        }

    }

    @Override
    public Boolean resetNodeInfo() {

        String prpsPath = GethHttpService.ROOT + File.separator + ".." + File.separator + "env.properties";
        Boolean deleted = new File(prpsPath).delete();
        restart();

        return deleted;
    }

    private void restart() {

        gethService.stopGeth();
        gethService.deletePid();
        gethService.start();

    }
    
    @Override
    public APIData getAPIData(Map data){
        
        APIData apiData = new APIData();
        Object id = data.get("id");
        if (id instanceof String) {
            apiData.setId((String) id);
        }
        apiData.setType("node");
        //handle empty IP address
        String ipAddress = (String) data.get("IP");
        //if no IP address is set, default is local host
        if( ipAddress != null && ("::".equalsIgnoreCase(ipAddress) || "0.0.0.0".equalsIgnoreCase(ipAddress) ) ){
            try {
                String ip = EEUtils.getLocalIP();
                if( ip != null ){
                    data.put("IP",ip);
                }
            } catch (APIException ex) {
                LOG.error(ex.getMessage());
            }
        }
        
        apiData.setAttributes(data);
        
        return apiData;
    }

}
