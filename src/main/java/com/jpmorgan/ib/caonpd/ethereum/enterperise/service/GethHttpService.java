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
    
    public static final String startXCommand = "/Users/I629630/workspace/go/eth-develop/jpm-goethereum/build/bin/geth";
    public static final String startWinCommand = "";
    
    public String executeGethCall(String json);
    public void startGeth();
    
}
