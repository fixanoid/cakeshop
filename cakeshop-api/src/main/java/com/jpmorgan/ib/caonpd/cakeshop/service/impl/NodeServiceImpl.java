package com.jpmorgan.ib.caonpd.cakeshop.service.impl;

import com.google.common.base.Joiner;
import com.jpmorgan.ib.caonpd.cakeshop.bean.AdminBean;
import com.jpmorgan.ib.caonpd.cakeshop.bean.GethConfigBean;
import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.model.Node;
import com.jpmorgan.ib.caonpd.cakeshop.model.NodeInfo;
import com.jpmorgan.ib.caonpd.cakeshop.model.Peer;
import com.jpmorgan.ib.caonpd.cakeshop.service.GethHttpService;
import com.jpmorgan.ib.caonpd.cakeshop.service.NodeService;
import com.jpmorgan.ib.caonpd.cakeshop.util.EEUtils;
import com.jpmorgan.ib.caonpd.cakeshop.util.AbiUtils;
import com.jpmorgan.ib.caonpd.cakeshop.util.EEUtils.IP;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

@Service
public class NodeServiceImpl implements NodeService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Value("${config.path}")
    private String CONFIG_ROOT;

    @Autowired
    private GethHttpService gethService;

    @Autowired
    private GethConfigBean gethConfig;

    @Override
    public Node get() throws APIException {

        Node node = new Node();

        Map<String, Object> data = null;

        try {
            //check if node is available
            data = gethService.executeGethCall(AdminBean.ADMIN_NODE_INFO, null);

            node.setRpcUrl(gethConfig.getRpcUrl());
            node.setDataDirectory(gethConfig.getDataDirPath());

            node.setId((String) data.get("id"));
            node.setStatus(StringUtils.isEmpty((String) data.get("id")) ? NodeService.NODE_NOT_RUNNING_STATUS : NodeService.NODE_RUNNING_STATUS);
            node.setNodeName((String) data.get("name"));

            String nodeURI = (String) data.get("enode");
            if (StringUtils.isNotEmpty(nodeURI)) {
                try {
                    URI uri = new URI(nodeURI);
                    String host = uri.getHost();
                    //if host or IP aren't set, then populate with localhost IP
                    if(StringUtils.isEmpty(host) || "[::]".equals(host) ||  "0.0.0.0".equalsIgnoreCase(host)){

                        try {
                            List<IP> ips = EEUtils.getAllIPs();
                            uri = new URI(uri.getScheme(), uri.getUserInfo(), ips.get(0).getAddr(), uri.getPort(), null, uri.getQuery(), null);
                            node.setNodeUrl(uri.toString());
                            node.setNodeIP(Joiner.on(",").join(ips));

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
            data = gethService.executeGethCall(AdminBean.ADMIN_MINER_MINING, null);
            Boolean mining = (Boolean)data.get(GethHttpServiceImpl.SIMPLE_RESULT);
            node.setMining(mining == null ? false : mining);

            //peer count
            data = gethService.executeGethCall(AdminBean.ADMIN_NET_PEER_COUNT, null);
            String peerCount = (String)data.get(GethHttpServiceImpl.SIMPLE_RESULT);
            node.setPeerCount(peerCount == null ? 0 : Integer.decode(peerCount));

            //get last block number
            data = gethService.executeGethCall(AdminBean.ADMIN_ETH_BLOCK_NUMBER, null);
            String blockNumber = (String)data.get(GethHttpServiceImpl.SIMPLE_RESULT);
            node.setLatestBlock(blockNumber == null ? 0 : Integer.decode(blockNumber));

            //get pending transactions
            data = gethService.executeGethCall(AdminBean.ADMIN_TXPOOL_STATUS, null);
            Integer pending = AbiUtils.hexToBigInteger((String) data.get("pending")).intValue();
            node.setPendingTxn(pending == null ? 0 : pending);

            try {
                node.setConfig(createNodeInfo());
            } catch (IOException e) {
                throw new APIException("Failed to read genesis block file", e);
            }

            node.setPeers(peers());

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

    private NodeInfo createNodeInfo() throws IOException {
        return new NodeInfo(gethConfig.getIdentity(), gethConfig.isMining(), gethConfig.getNetworkId(),
                gethConfig.getVerbosity(), gethConfig.getGenesisBlock(), gethConfig.getExtraParams());
    }

    @Override
    public NodeInfo update(
            Integer logLevel, Integer networkID, String identity, Boolean mining,
            String extraParams, String genesisBlock) throws APIException {

        boolean restart = false;
        boolean reset = false;

        if (networkID != null && networkID != gethConfig.getNetworkId()) {
            gethConfig.setNetworkId(networkID);
            restart = true;
        }

        if (StringUtils.isNotEmpty(identity) && !identity.contentEquals(gethConfig.getIdentity())) {
            gethConfig.setIdentity(identity);
            restart = true;
        }

        if (logLevel != null && logLevel != gethConfig.getVerbosity()) {
            gethConfig.setVerbosity(logLevel);
            if (!restart) {
                // make it live immediately
                gethService.executeGethCall(AdminBean.ADMIN_VERBOSITY, new Object[]{ logLevel });
            }
        }

        String currExtraParams = gethConfig.getExtraParams();
        if (extraParams != null && (currExtraParams == null || !extraParams.contentEquals(currExtraParams))) {
            gethConfig.setExtraParams(extraParams);
            restart = true;
        }

        try {
            if (StringUtils.isNotBlank(genesisBlock) && !genesisBlock.contentEquals(gethConfig.getGenesisBlock())) {
                gethConfig.setGenesisBlock(genesisBlock);
                reset = true;
            }
        } catch (IOException e) {
            throw new APIException("Failed to update genesis block", e);
        }

        if (mining != null && mining != gethConfig.isMining()) {
            gethConfig.setMining(mining);

            if (!restart) {
                // make it live immediately
                if (mining == true) {
                    gethService.executeGethCall(AdminBean.ADMIN_MINER_START, new String[]{"1"});
                } else {
                    gethService.executeGethCall(AdminBean.ADMIN_MINER_STOP, null);
                }
            }
        }

        NodeInfo nodeInfo;
        try {
            gethConfig.save();
            nodeInfo = createNodeInfo();
        } catch (IOException e) {
            LOG.error("Error saving config", e);
            throw new APIException("Error saving config", e);
        }

        // TODO reset/restart in background?
        if (reset) {
            gethService.reset();
        } else if (restart) {
            restart();
        }

        return nodeInfo;
    }

    @Override
    public Boolean reset() {
        try {
            gethConfig.initFromVendorConfig();
        } catch (IOException e) {
            LOG.warn("Failed to reset config file", e);
            return false;
        }

        restart();
        return true;
    }

    private void restart() {
        gethService.stop();
        gethService.deletePid();
        gethService.start();
    }

    @Override
    public List<Peer> peers() throws APIException{
        String args[] = null;
        Map data = gethService.executeGethCall(AdminBean.ADMIN_PEERS, args);
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
            String id = (String)data.get("id");
            peer.setId(id);
            String name = (String)data.get("name");
            peer.setNodeName(name);
            String remoteAddress = (String)((Map<String, Object>) data.get("network")).get("remoteAddress");
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