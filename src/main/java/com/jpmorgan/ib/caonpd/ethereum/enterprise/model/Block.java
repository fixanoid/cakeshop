package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Block {

    public static final String API_DATA_TYPE = "block";

    private Long number = null;
    private String id = null;
    private String parentId = null;
    private String nonce = null;
    private String sha3Uncles = null;
    private String logsBloom = null;
    private String transactionsRoot = null;
    private String stateRoot = null;
    private String miner = null;
    private Long difficulty = null;
    private Long totalDifficulty = null;
    private String extraData = null;
    private Long gasLimit = null;
    private Long gasUsed = null;
    private Long timestamp = null;
    private List<String> transactions = new ArrayList<String>();
    private List<String> uncles = new ArrayList<String>();

    /**
     * Block number
     **/
    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    /**
     * id of the block
     **/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * id of the parent block
     **/
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * hash of the generated proof-of-work (if avail)
     **/
    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * SHA3 of the uncles data in the block (32 bytes)
     **/
    public String getSha3Uncles() {
        return sha3Uncles;
    }

    public void setSha3Uncles(String sha3Uncles) {
        this.sha3Uncles = sha3Uncles;
    }

    /**
     * the bloom filter for the logs of the block (256 bytes)
     **/
    public String getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    /**
     * the root of the transaction trie of the block (32 bytes)
     **/
    public String getTransactionsRoot() {
        return transactionsRoot;
    }

    public void setTransactionsRoot(String transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
    }

    /**
     * the root of the final state trie of the block (32 bytes)
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
     **/
    public String getMiner() {
        return miner;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    /**
     * integer of the difficulty of this block
     **/
    public Long getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Long difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * integer of the total difficulty of the chain until this block
     **/
    public Long getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(Long totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    /**
     * the \"extra data\" field for this block
     **/
    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    /**
     * the maximum gas allowed in this block
     **/
    public Long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(Long gasLimit) {
        this.gasLimit = gasLimit;
    }

    /**
     * the total gas used by all transactions in this block
     **/
    public Long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(Long gasUsed) {
        this.gasUsed = gasUsed;
    }

    /**
     * the unix timestamp for when the block was collated
     **/
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Array of transaction hashes
     **/
    public List<String> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<String> transactions) {
        this.transactions = transactions;
    }

    /**
     * Array of uncle hashes
     **/
    public List<String> getUncles() {
        return uncles;
    }

    public void setUncles(List<String> uncles) {
        this.uncles = uncles;
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
     */
    @Override
    public boolean equals(Object obj) {
        Block otherBlock = (Block) obj;
        return this.id.contentEquals(otherBlock.getId());
    }
}
