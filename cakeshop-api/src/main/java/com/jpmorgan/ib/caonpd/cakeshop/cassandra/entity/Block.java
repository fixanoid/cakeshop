package com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity;


import com.jpmorgan.ib.caonpd.cakeshop.model.APIData;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
            name = "id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String id;
    
    @PrimaryKeyColumn(
            name = "blockNumber",
            ordinal = 1,
            type = PrimaryKeyType.CLUSTERED,
            ordering = Ordering.DESCENDING)
    private BigInteger blockNumber;
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
    private BigInteger difficulty;
    @Column
    private BigInteger totalDifficulty;
    @Column
    private String extraData;
    @Column
    private BigInteger gasLimit;
    @Column
    private BigInteger gasUsed;
    @Column
    private BigInteger timestamp;
    @Column
    private List<String> transactions = new ArrayList<>();
    @Column
    private List<String> uncles = new ArrayList<>();


    /**
     * Block number
     * @return 
     **/
    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger number) {
        this.blockNumber = number;
    }
    
    public Block withBlockNunmer(BigInteger number) {
        this.blockNumber = number;
        return this;
    }

    /**
     * id of the block
     * @return 
     **/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public Block withId(String id) {
        this.id = id;
        return this;
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
    
    public Block withParentId(String parentId) {
        this.parentId = parentId;
        return this;
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

    public Block withNonce(String nonce) {
        this.nonce = nonce;
        return this;
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
    
    public Block withSha3Uncles(String sha3Uncles) {
        this.sha3Uncles = sha3Uncles;
        return this;
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
    
    public Block withLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
        return this;
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

    public Block withTransactionsRoot(String transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
        return this;
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

    public Block withStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
        return this;
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
    
    public Block withMiner(String miner) {
        this.miner = miner;
        return this;
    }

    /**
     * integer of the difficulty of this block
     * @return 
     **/
    public BigInteger getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(BigInteger difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * integer of the total difficulty of the chain until this block
     * @return 
     **/
    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(BigInteger totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }
    
    public Block withTotalDifficulty(BigInteger totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
        return this;
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

    public Block withExtraData(String extraData) {
        this.extraData = extraData;
        return this;
    }
    
    /**
     * the maximum gas allowed in this block
     * @return 
     **/
    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public Block withGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
        return this;
    }
    
    /**
     * the total gas used by all transactions in this block
     * @return 
     **/
    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    public Block withGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
        return this;
    }
    
    /**
     * the unix timestamp for when the block was collated
     * @return 
     **/
    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }
    
    public Block withTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * @return the transactions
     */
    public List<String> getTransactions() {
        return transactions;
    }
    
    public Block withTransactions(List<String> transactions) {
        this.transactions = transactions;
        return this;
    }

    /**
     * @param transactions the transactions to set
     */
    public void setTransactions(List<String> transactions) {
        this.transactions = transactions;
    }

    /**
     * @return the uncles
     */
    public List<String> getUncles() {
        return uncles;
    }

    /**
     * @param uncles the uncles to set
     */
    public void setUncles(List<String> uncles) {
        this.uncles = uncles;
    }
    
    public Block withUncles(List<String> uncles) {
        this.uncles = uncles;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public APIData toAPIData() {
        APIData data = new APIData();
        data.setId(getId());
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
        return this.id.contentEquals(otherBlock.getId());
    }
}
