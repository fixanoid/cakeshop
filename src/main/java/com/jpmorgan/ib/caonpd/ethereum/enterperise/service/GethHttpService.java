/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterperise.service;

/**
 *
 * @author I629630
 */
public interface GethHttpService {
    
    public static final String startXCommand = System.getProperty("user.dir") + "/bin/geth";
    public static final String startWinCommand = System.getProperty("user.dir") + "/bin/geth.exe";
    
    public String executeGethCall(String json);
    public void startGeth(String command, String genesisDir);
    
}
