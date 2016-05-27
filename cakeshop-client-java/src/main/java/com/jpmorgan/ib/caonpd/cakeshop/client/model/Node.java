package com.jpmorgan.ib.caonpd.cakeshop.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-26T13:00:18.987-04:00")
public class Node   {

    private String status = null;
    private String id = null;
    private String nodeName = null;
    private String nodeUrl = null;
    private String nodeIP = null;
    private Integer peerCount = null;
    private Integer pendingTxn = null;
    private Boolean mining = null;
    private Integer latestBlock = null;


    /**
     * Status of the node, it has two values \"running\" or \"stopped\"
     **/
    public Node status(String status) {
        this.status = status;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Status of the node, it has two values \"running\" or \"stopped\"")
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }


    /**
     * Unique Node ID
     **/
    public Node id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Unique Node ID")
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    /**
     * Friendly node name (includes client and version info)
     **/
    public Node nodeName(String nodeName) {
        this.nodeName = nodeName;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Friendly node name (includes client and version info)")
    @JsonProperty("nodeName")
    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }


    /**
     * enode URI (Includes host IP and port number)
     **/
    public Node nodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
        return this;
    }

    @ApiModelProperty(example = "null", value = "enode URI (Includes host IP and port number)")
    @JsonProperty("nodeUrl")
    public String getNodeUrl() {
        return nodeUrl;
    }
    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }


    /**
     * IP address of the node
     **/
    public Node nodeIP(String nodeIP) {
        this.nodeIP = nodeIP;
        return this;
    }

    @ApiModelProperty(example = "null", value = "IP address of the node")
    @JsonProperty("nodeIP")
    public String getNodeIP() {
        return nodeIP;
    }
    public void setNodeIP(String nodeIP) {
        this.nodeIP = nodeIP;
    }


    /**
     * Number of peers connected to the node
     **/
    public Node peerCount(Integer peerCount) {
        this.peerCount = peerCount;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Number of peers connected to the node")
    @JsonProperty("peerCount")
    public Integer getPeerCount() {
        return peerCount;
    }
    public void setPeerCount(Integer peerCount) {
        this.peerCount = peerCount;
    }


    /**
     * Number of transaction in the queue and haven't been mined
     **/
    public Node pendingTxn(Integer pendingTxn) {
        this.pendingTxn = pendingTxn;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Number of transaction in the queue and haven't been mined")
    @JsonProperty("pendingTxn")
    public Integer getPendingTxn() {
        return pendingTxn;
    }
    public void setPendingTxn(Integer pendingTxn) {
        this.pendingTxn = pendingTxn;
    }


    /**
     * Indicates weather the miner is running or not. It has a true or false values.
     **/
    public Node mining(Boolean mining) {
        this.mining = mining;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Indicates weather the miner is running or not. It has a true or false values.")
    @JsonProperty("mining")
    public Boolean getMining() {
        return mining;
    }
    public void setMining(Boolean mining) {
        this.mining = mining;
    }


    /**
     * A number indicating the most recent block that has been mined.
     **/
    public Node latestBlock(Integer latestBlock) {
        this.latestBlock = latestBlock;
        return this;
    }

    @ApiModelProperty(example = "null", value = "A number indicating the most recent block that has been mined.")
    @JsonProperty("latestBlock")
    public Integer getLatestBlock() {
        return latestBlock;
    }
    public void setLatestBlock(Integer latestBlock) {
        this.latestBlock = latestBlock;
    }



    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Node node = (Node) o;
        return Objects.equals(this.status, node.status) &&
                Objects.equals(this.id, node.id) &&
                Objects.equals(this.nodeName, node.nodeName) &&
                Objects.equals(this.nodeUrl, node.nodeUrl) &&
                Objects.equals(this.nodeIP, node.nodeIP) &&
                Objects.equals(this.peerCount, node.peerCount) &&
                Objects.equals(this.pendingTxn, node.pendingTxn) &&
                Objects.equals(this.mining, node.mining) &&
                Objects.equals(this.latestBlock, node.latestBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, id, nodeName, nodeUrl, nodeIP, peerCount, pendingTxn, mining, latestBlock);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Node {\n");

        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    nodeName: ").append(toIndentedString(nodeName)).append("\n");
        sb.append("    nodeUrl: ").append(toIndentedString(nodeUrl)).append("\n");
        sb.append("    nodeIP: ").append(toIndentedString(nodeIP)).append("\n");
        sb.append("    peerCount: ").append(toIndentedString(peerCount)).append("\n");
        sb.append("    pendingTxn: ").append(toIndentedString(pendingTxn)).append("\n");
        sb.append("    mining: ").append(toIndentedString(mining)).append("\n");
        sb.append("    latestBlock: ").append(toIndentedString(latestBlock)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

