package com.jpmorgan.ib.caonpd.ethereum.enterprise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.APIData;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Node;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.NodeInfo;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.Peer;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.NodeService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.EEUtils;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

@Service
public class NodeServiceImpl implements NodeService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NodeServiceImpl.class);
    public static final String ADMIN_MINER_START = "miner_start";
    public static final String ADMIN_MINER_STOP = "miner_stop";

    @Value("${config.path}")
    private String CONFIG_ROOT;

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
    public NodeInfo update(Integer logLevel,Integer networkID,String identity,Boolean mining) throws APIException {

        Map<String, Object> data=null;
        Map<String,String> nodeProp = null;

        String args[] = null;

        if(logLevel != null || networkID != null || !StringUtils.isEmpty(identity)){

            nodeProp = new HashMap<>();

            if(logLevel != null){
                nodeProp.put("geth.verbosity", logLevel.toString());
            }
            if(networkID != null){
                nodeProp.put("geth.networkid", networkID.toString());

            }
            if(StringUtils.isNotEmpty(identity)){
                nodeProp.put("geth.identity", identity);
            }

            gethService.setNodeInfo(identity, mining, logLevel,networkID);
            updateNodeInfo(nodeProp,true);

        }

        if(mining != null){
            nodeProp = new HashMap<>();
            nodeProp.put("geth.mining",mining.toString());
            if(mining == true){
                args = new String[]{"1"};
                data = gethService.executeGethCall(ADMIN_MINER_START,args);
            }else{
                data = gethService.executeGethCall(ADMIN_MINER_STOP,args);
            }

            gethService.setNodeInfo(identity, mining, logLevel,networkID);
            updateNodeInfo(nodeProp,false);

        }
        return gethService.getNodeInfo();
    }


    @Override
    public void updateNodeInfo(Map<String, String> newProps,Boolean requiresRestart) throws APIException {

        String prpsPath = FileUtils.expandPath(CONFIG_ROOT, "env.properties");
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
                if(requiresRestart)
                    restart();
            }
        } catch (IOException ex) {

            LOG.error(ex.getMessage());
            throw new APIException(ex.getMessage());

        }

    }

    @Override
    public Boolean resetNodeInfo() {

        String prpsPath = FileUtils.expandPath(CONFIG_ROOT, "env.properties");
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

    @Override
    public List<Peer> peers() throws APIException{
        String args[] = null;
        Map data = gethService.executeGethCall(AdminBean.ADMIN_PEERS,args );
        List peers = null;
        List<Peer> peerList = new ArrayList<>();

        if(data != null){

            peers =(List) data.get("_result");
            if(peers != null){
                for (Iterator iterator = peers.iterator(); iterator.hasNext();) {
                    Map peerMap = (Map)iterator.next();
                    Peer peer = populateNode(peerMap);
                    peerList.add(peer);
                }
            }
        }

        return peerList;
    }

    private Peer populateNode(Map data){
        Peer peer = null;
        URI uri = null;

        if(data != null){

            peer = new Peer();
            peer.setStatus("running");
            String id = (String)data.get("ID");
            peer.setId(id);
            String name = (String)data.get("Name");
            peer.setNodeName(name);
            String remoteAddress = (String)data.get("RemoteAddress");
            try {
                URI remoteURI = new URI("enode://" + remoteAddress);

                if(remoteURI.getHost() != null && remoteURI.getPort() != -1){

                    uri = new URI("enode",id, remoteURI.getHost(),remoteURI.getPort(), null, null, null);
                    peer.setNodeUrl(uri.toString());
                    peer.setNodeIP(remoteURI.getHost());
                }

            } catch (URISyntaxException ex) {
                LOG.error("error parsing Peer Address ",ex.getMessage());
                peer.setNodeUrl("");
            }

        }

        return peer;
    }

}
