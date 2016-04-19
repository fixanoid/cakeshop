package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;

import java.io.Serializable;

/**
 *
 * @author i629630
 */
public class TransactionCacheModel implements Serializable {

    private String data;
    private Long timeCreated;

    public TransactionCacheModel (String data, Long timeCreated) {
        this.data = data;
        this.timeCreated = timeCreated;
    }

    /**
     * @return the destination
     */
    public String getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * @return the timeCreated
     */
    public Long getTimeCreated() {
        return timeCreated;
    }

    /**
     * @param timeCreated the timeCreated to set
     */
    public void setTimeCreated(Long timeCreated) {
        this.timeCreated = timeCreated;
    }

}
