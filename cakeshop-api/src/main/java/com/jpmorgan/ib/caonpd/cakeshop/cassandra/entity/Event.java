package com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity;

import com.jpmorgan.ib.caonpd.cakeshop.util.StringUtils;
import java.math.BigInteger;
import java.util.List;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

@Table(value="events")
public class Event {

    public static final String API_DATA_TYPE = "event";

    @PrimaryKeyColumn(
            name = "id",
            ordinal = 4,
            type = PrimaryKeyType.CLUSTERED,
            ordering = Ordering.DESCENDING)
    private BigInteger id;
   
    @PrimaryKeyColumn(
            name = "blockNumber", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private BigInteger blockNumber;
    @PrimaryKeyColumn(
            name = "transactionId", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
    private String transactionId;
    @PrimaryKeyColumn(
            name = "contractId", ordinal = 2, type = PrimaryKeyType.PARTITIONED)
    private String contractId;
    @PrimaryKeyColumn(
            name = "blockId", ordinal = 3, type = PrimaryKeyType.PARTITIONED)
    private String blockId;
    @Column
    private BigInteger logIndex;    
    @Column
    private String name;
    @Column
    private List<String> data;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    public BigInteger getLogIndex() {
        return logIndex;
    }

    public void setLogIndex(BigInteger logIndex) {
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

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return StringUtils.toString(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
