package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Node  {

    /**
     * Node status
     */
    private String status;

    /**
     * Node ID
     */
    private String id;

    /**
     * Number of peers connected to node
     */
    private int peerCount;

    /**
     * Last mined block
     */
    private long latestBlock;

    /**
     * Pending transactions in txpool
     */
    private long pendingTxn;

    /**
     * True if miner is running on the node
     */
    private boolean mining;

    /**
     * Node Address
     */
    private String nodeUrl;

    /**
     * Node Name
     */
    private String nodeName;

    /**
     * Node IP
     */
    private String nodeIP;

    public String getStatus() {
         return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPeerCount() {
        return peerCount;
    }

    public void setPeerCount(int peerCount) {
        this.peerCount = peerCount;
    }

    public long getLatestBlock() {
        return latestBlock;
    }

    public void setLatestBlock(long latestBlock) {
        this.latestBlock = latestBlock;
    }

    public long getPendingTxn() {
        return pendingTxn;
    }

    public void setPendingTxn(long pendingTxn) {
        this.pendingTxn = pendingTxn;
    }

    public boolean isMining() {
        return mining;
    }

    public void setMining(boolean mining) {
        this.mining = mining;
    }

     public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setNodeIP(String ip) {
        this.nodeIP = ip;
    }

    public String getNodeIP() {
        return nodeIP;
    }

    @Override
    public String toString()  {
      StringBuilder sb = new StringBuilder();
      sb.append("{\n");
      sb.append("\"id\":").append("\"").append(id).append("\",").append("\n");
      sb.append("\"status\":").append("\"").append(status).append("\",").append("\n");
      sb.append("\"peers\":").append(peerCount).append(",").append("\n");
      sb.append("\"latestBlock\":").append(latestBlock).append(",").append("\n");
      sb.append("\"pendingTxn\":").append(pendingTxn).append(",").append("\n");
      sb.append("\"nodeUrl\":").append(nodeUrl).append(",").append("\n");
      sb.append("\"nodeName\":").append(nodeName).append(",").append("\n");
      sb.append("\"nodeIP\":").append(nodeIP).append(",").append("\n");
      sb.append("\"mining\":").append(mining).append("\n");
      sb.append("}\n");
      return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

}
