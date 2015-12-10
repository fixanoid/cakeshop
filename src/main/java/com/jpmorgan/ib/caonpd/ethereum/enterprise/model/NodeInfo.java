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
    private Boolean mining;
    private Integer networkid;
    private Integer verbosity;
    
    public NodeInfo (String identity, Boolean mining, Integer networkid, Integer verbosity) {
        this.identity = identity;
        this.mining = mining;
        this.networkid = networkid;
        this.verbosity = verbosity;
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
    public Boolean getMining() {
        return mining;
    }

    /**
     * @param mining the mining to set
     */
    public void setMining(Boolean mining) {
        this.mining = mining;
    }

    /**
     * @return the networkid
     */
    public Integer getNetworkid() {
        return networkid;
    }

    /**
     * @param networkid the networkid to set
     */
    public void setNetworkid(Integer networkid) {
        this.networkid = networkid;
    }

    /**
     * @return the verbosity
     */
    public Integer getVerbosity() {
        return verbosity;
    }

    /**
     * @param verbosity the verbosity to set
     */
    public void setVerbosity(Integer verbosity) {
        this.verbosity = verbosity;
    }
    
    public APIData toAPIData() {
        APIData data = new APIData();
        data.setId(null);
        data.setType("node-info");
        data.setAttributes(this);
        return data;
    }
    
}
