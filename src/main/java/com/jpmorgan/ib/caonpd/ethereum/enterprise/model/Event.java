package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.db.JpaJsonConverter;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.RpcUtil;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="EVENTS", schema="PUBLIC")
public class Event {

    public static final String API_DATA_TYPE = "event";

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private long id;

    private String blockId;
    private Long blockNumber;

    private long logIndex;
    private String transactionId;
    private String contractId;

    @Basic
    //@Lob
    @Column(length = Integer.MAX_VALUE)
    @Convert(converter = JpaJsonConverter.class)
    private Object[] data;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public long getLogIndex() {
        return logIndex;
    }

    public void setLogIndex(long logIndex) {
        this.logIndex = logIndex;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public Object[] getData() {
        return data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return RpcUtil.toString(this);
    }

}
