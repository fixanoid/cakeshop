/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.NodeInfo;

import java.util.Map;

/**
 *
 * @author I629630
 */
public interface GethHttpService {

    public static final String GETH_API_VERSION = "2.0";
    public static final String USER_ID = "enterprise-ethereum";

    public String executeGethCall(String json) throws APIException;
    public Map<String, Object> executeGethCall(String funcName, Object[] args) throws APIException;

    public Boolean start(String... additionalParams);

    public Boolean stopGeth();

    public Boolean deleteEthDatabase(String eth_datadir);

    public Boolean deletePid();

    public void setNodeInfo(String identity, Boolean mining, Integer verbosity, Integer networkid);

    public NodeInfo getNodeInfo();
}
