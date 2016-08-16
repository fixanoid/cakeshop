package com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity;


import com.jpmorgan.ib.caonpd.cakeshop.model.APIData;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.cassandra.core.Ordering;

import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;


@Table(value = "block")
public class Block {

    public static final String API_DATA_TYPE = "block";

    @PrimaryKeyColumn(
            name = "id",
            ordinal = 1,
            type = PrimaryKeyType.CLUSTERED,
            ordering = Ordering.DESCENDING)
    private UUID id;
    @PrimaryKeyColumn(
            name = "block_number", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private Long blockNumber;
    @Column
    private String parentId;
    @Column
    private String nonce;
    @Column
    private String sha3Uncles;
    @Column
    private String logsBloom;
    @Column
    private String transactionsRoot;
    @Column
    private String stateRoot;
    @Column
    private String miner;
    @Column
    private Long difficulty;
    @Column
    private Long totalDifficulty;
    @Column
    private String extraData;
    @Column
    private Long gasLimit;
    @Column
    private Long gasUsed;
    @Column
    private Long timestamp;


    /**
     * Block number
     * @return 
     **/
    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long number) {
        this.blockNumber = number;
    }

    /**
     * id of the block
     * @return 
     **/
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * id of the parent block
     * @return 
     **/
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * hash of the generated proof-of-work (if avail)
     * @return 
     **/
    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * SHA3 of the uncles data in the block (32 bytes)
     * @return 
     **/
    public String getSha3Uncles() {
        return sha3Uncles;
    }

    public void setSha3Uncles(String sha3Uncles) {
        this.sha3Uncles = sha3Uncles;
    }

    /**
     * the bloom filter for the logs of the block (256 bytes)
     * @return 
     **/
    public String getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    /**
     * the root of the transaction trie of the block (32 bytes)
     * @return 
     **/
    public String getTransactionsRoot() {
        return transactionsRoot;
    }

    public void setTransactionsRoot(String transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
    }

    /**
     * the root of the final state trie of the block (32 bytes)
     * @return 
     **/
    public String getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    /**
     * the address of the beneficiary to whom the mining rewards were given (20
     * bytes)
     * @return 
     **/
    public String getMiner() {
        return miner;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    /**
     * integer of the difficulty of this block
     * @return 
     **/
    public Long getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Long difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * integer of the total difficulty of the chain until this block
     * @return 
     **/
    public Long getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(Long totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    /**
     * the \"extra data\" field for this block
     * @return 
     **/
    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    /**
     * the maximum gas allowed in this block
     * @return 
     **/
    public Long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(Long gasLimit) {
        this.gasLimit = gasLimit;
    }

    /**
     * the total gas used by all transactions in this block
     * @return 
     **/
    public Long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(Long gasUsed) {
        this.gasUsed = gasUsed;
    }

    /**
     * the unix timestamp for when the block was collated
     * @return 
     **/
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public APIData toAPIData() {
        APIData data = new APIData();
        data.setId(getId().toString());
        data.setType(API_DATA_TYPE);
        data.setAttributes(this);
        return data;
    }

    /**
     * Blocks are considered equal if their IDs (hashes) are the same
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        Block otherBlock = (Block) obj;
        return this.id.toString().contentEquals(otherBlock.getId().toString());
    }
}
