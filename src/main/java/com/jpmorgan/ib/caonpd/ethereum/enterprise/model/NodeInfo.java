/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;

/**
 *
 * @author I629630
 */
public class NodeInfo {
    
    private String identity;
    private Boolean committingTransactions;
    private Integer networkId;
    private Integer logLevel;
    
    public NodeInfo (String identity, Boolean mining, Integer networkid, Integer verbosity) {
        this.identity = identity;
        this.committingTransactions = mining;
        this.networkId = networkid;
        this.logLevel = verbosity;
    }

    /**
     * @return the identity
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * @param identity the identity to set
     */
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    /**
     * @return the mining
     */
    public Boolean getCommittingTransactions() {
        return committingTransactions;
    }

    /**
     * @param committingTransactions the mining to set
     */
    public void setCommittingTransactions(Boolean committingTransactions) {
        this.committingTransactions = committingTransactions;
    }

    /**
     * @return the networkId
     */
    public Integer getNetworkId() {
        return networkId;
    }

    /**
     * @param networkId the networkId to set
     */
    public void setNetworkid(Integer networkId) {
        this.networkId = networkId;
    }

    /**
     * @return the logLevel
     */
    public Integer getLogLevel() {
        return logLevel;
    }

    /**
     * @param logLevel the verbosity to set
     */
    public void setLogLevel(Integer logLevel) {
        this.logLevel = logLevel;
    }
    
    public APIData toAPIData() {
        APIData data = new APIData();
        data.setId(null);
        data.setType("node-info");
        data.setAttributes(this);
        return data;
    }
    
}
