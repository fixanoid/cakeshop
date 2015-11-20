/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.service;

import java.util.List;

/**
 *
 * @author I629630
 */
public interface GethHttpService {
    
    public static final String startXCommand = "bin/linux/geth";
    public static final String startWinCommand = "bin/win/geth.exe";
    public static final String startMacCommand = "bin/mac/geth";
    
    public String executeGethCall(String json);
    public Boolean startGeth(String command, String genesisDir, String eth_datadir, List<String> additionalParams);
    public Boolean stopGeth ();
    public Boolean deletEthDatabase(String eth_datadir);
    
}
