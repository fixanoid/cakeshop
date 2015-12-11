/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import java.util.List;
import java.util.Map;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;

/**
 *
 * @author I629630
 */
public interface GethHttpService {

    public static final String startXCommand = "bin/linux/geth";
    public static final String startWinCommand = "bin/win/geth.exe";
    public static final String startMacCommand = "bin/mac/geth";
    public static final String GETH_API_VERSION = "2.0";
    public static final String USER_ID = "enterprise-ethereum";
    public static final String ROOT = GethHttpService.class.getClassLoader().getResource("").getPath().replaceAll("/WEB-INF/classes/", "");


    public String executeGethCall(String json) throws APIException;
    public Map<String, Object> executeGethCall(String funcName, Object[] args) throws APIException;

    public Boolean startGeth(String command, String genesisDir, String eth_datadir, List<String> additionalParams);
    public Boolean stopGeth ();
    public Boolean deletEthDatabase(String eth_datadir);
    public Boolean deletePid();
    public void start();
    public void setNodeInfo(String identity, Boolean mining, Integer verbosity, Integer networkid);

}
