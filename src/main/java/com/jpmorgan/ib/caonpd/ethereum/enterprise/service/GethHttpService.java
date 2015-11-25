/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

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


    public String executeGethCall(String json);
    public Map<String, Object> executeGethCall(String funcName, Object[] args) throws JsonParseException, JsonMappingException, IOException;

    public Boolean startGeth(String command, String genesisDir, String eth_datadir, List<String> additionalParams);
    public Boolean stopGeth ();
    public Boolean deletEthDatabase(String eth_datadir);
    public void deletePid();

}
