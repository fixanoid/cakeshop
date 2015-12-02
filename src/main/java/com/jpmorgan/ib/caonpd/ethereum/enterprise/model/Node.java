package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;


public class Node  {

    private String status;
    private int peerCount;
    private long latestBlock;
    private long pendingTxn;
    private boolean mining;

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
 
    @Override
    public String toString()  {
      StringBuilder sb = new StringBuilder();
      sb.append("{\n");
      sb.append("\"status\":").append("\"").append(status).append("\",").append("\n");
      sb.append("\"peers\":").append(peerCount).append(",").append("\n");
      sb.append("\"latestBlock\":").append(latestBlock).append(",").append("\n");
      sb.append("\"pendingTxn\":").append(pendingTxn).append(",").append("\n");
      sb.append("\"mining\":").append(mining).append("\n");
      sb.append("}\n");
      return sb.toString();
    }
  
}
