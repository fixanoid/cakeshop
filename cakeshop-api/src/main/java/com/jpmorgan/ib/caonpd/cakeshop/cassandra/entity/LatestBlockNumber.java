package com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity;

import java.math.BigInteger;
//import org.springframework.cassandra.core.PrimaryKeyType;
//import org.springframework.data.cassandra.mapping.Column;
//import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
//import org.springframework.data.cassandra.mapping.Table;

/**
 *
 * @author I629630
 */
//@Table(value = "latest_block")
public class LatestBlockNumber {
    
//    @PrimaryKeyColumn(
//            name = "id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private final BigInteger id = new BigInteger ("0");
//    @Column
    private BigInteger blockNumber;

    /**
     * @return the id
     */
    public BigInteger getId() {
        return id;
    }

    /**
     * @return the blockNumber
     */
    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    /**
     * @param blockNumber the blockNumber to set
     */
    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }
    
    
    
}
