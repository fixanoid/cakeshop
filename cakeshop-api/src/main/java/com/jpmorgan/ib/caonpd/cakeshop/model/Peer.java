package com.jpmorgan.ib.caonpd.cakeshop.model;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Peer  {

    /**
    *Node status
    */
    private String status;
    /**
    *Node ID
    */
    private String id;

    /*
    *Node Address
    */
    private String nodeUrl;
    /*
    *Node Name
    */
    private String nodeName;
    /*
    *Node IP
    */
    private String nodeIP;

    public String getStatus() {
         return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
      sb.append("\"nodeUrl\":").append(nodeUrl).append(",").append("\n");
      sb.append("\"nodeName\":").append(nodeName).append(",").append("\n");
      sb.append("\"nodeIP\":").append(nodeIP);
      sb.append("}\n");
      return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

}
