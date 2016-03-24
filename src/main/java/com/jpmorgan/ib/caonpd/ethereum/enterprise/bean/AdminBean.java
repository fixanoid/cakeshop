/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.bean;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author n631539
 */
public class AdminBean {

    public static final String ADMIN_ADD_PEER_KEY = "add_peer";
    public static final String ADMIN_ADD_PEER = "admin_addPeer";
    public static final String ADMIN_PEERS_KEY = "peers";
    public static final String ADMIN_PEERS = "admin_peers";
    public static final String ADMIN_NODE_INFO_KEY = "get";
    public static final String ADMIN_NODE_INFO = "admin_nodeInfo";
    public static final String ADMIN_VERBOSITY_KEY = "verbosity";
    public static final String ADMIN_VERBOSITY = "debug_verbosity";
    public static final String ADMIN_DATADIR_KEY = "datadir";
    public static final String ADMIN_DATADIR = "admin_datadir";
    public static final String ADMIN_MINER_START_KEY = "start";
    public static final String ADMIN_MINER_START = "miner_start";
    public static final String ADMIN_MINER_STOP_KEY = "stop";
    public static final String ADMIN_MINER_STOP = "miner_stop";
    public static final String ADMIN_MINER_MINING_KEY = "mining";
    public static final String ADMIN_MINER_MINING = "eth_mining";
    public static final String ADMIN_PEER_ADD="admin_addPeer";
    public static final String ADMIN_PEER_ADD_KEY="add_peer";
    public static final String ADMIN_NET_PEER_COUNT="net_peerCount";
    public static final String ADMIN_ETH_BLOCK_NUMBER="eth_blockNumber";
    public static final String ADMIN_TXPOOL_STATUS = "txpool_status";
    public static final String PERSONAL_LIST_ACCOUNTS_KEY= "list_accounts";
    public static final String PERSONAL_LIST_ACCOUNTS= "personal_listAccounts";
    public static final String PERSONAL_GET_ACCOUNT_BALANCE = "eth_getBalance";


    private Map<String,String> functionNames = new HashMap();

    public AdminBean(){
        functionNames.put(ADMIN_ADD_PEER_KEY,ADMIN_ADD_PEER);
        functionNames.put(ADMIN_PEERS_KEY,ADMIN_PEERS);
        functionNames.put(ADMIN_NODE_INFO_KEY,ADMIN_NODE_INFO);
        functionNames.put(ADMIN_VERBOSITY_KEY,ADMIN_VERBOSITY);
        functionNames.put(ADMIN_DATADIR_KEY,ADMIN_DATADIR);
        functionNames.put(ADMIN_MINER_START_KEY,ADMIN_MINER_START);
        functionNames.put(ADMIN_MINER_STOP_KEY,ADMIN_MINER_STOP);
        functionNames.put(ADMIN_MINER_MINING_KEY,ADMIN_MINER_MINING);
        functionNames.put(ADMIN_ADD_PEER_KEY,ADMIN_ADD_PEER);
    }

    /**
     * @return the functionNames
     */
    public Map<String,String> getFunctionNames() {
        return functionNames;
    }

    /**
     * @param functionNames the functionNames to set
     */
    public void setFunctionNames(Map<String,String> functionNames) {
        this.functionNames = functionNames;
    }

}
