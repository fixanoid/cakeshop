package com.jpmorgan.ib.caonpd.cakeshop.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-26T13:00:18.987-04:00")
public class NodeInfo   {

    private String identity = null;
    private Boolean committingTransactions = null;
    private Integer networkId = null;
    private Integer logLevel = null;


    /**
     * Friendly node name which gets included in the full node Name
     **/
    public NodeInfo identity(String identity) {
        this.identity = identity;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Friendly node name which gets included in the full node Name")
    @JsonProperty("identity")
    public String getIdentity() {
        return identity;
    }
    public void setIdentity(String identity) {
        this.identity = identity;
    }


    /**
     * Indicates whether the node is commiting transactions or not
     **/
    public NodeInfo committingTransactions(Boolean committingTransactions) {
        this.committingTransactions = committingTransactions;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Indicates whether the node is commiting transactions or not")
    @JsonProperty("committingTransactions")
    public Boolean getCommittingTransactions() {
        return committingTransactions;
    }
    public void setCommittingTransactions(Boolean committingTransactions) {
        this.committingTransactions = committingTransactions;
    }


    /**
     * Network identifier
     **/
    public NodeInfo networkId(Integer networkId) {
        this.networkId = networkId;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Network identifier")
    @JsonProperty("networkId")
    public Integer getNetworkId() {
        return networkId;
    }
    public void setNetworkId(Integer networkId) {
        this.networkId = networkId;
    }


    /**
     * Logging verbosity: 0-6 (0=silent, 1=error, 2=warn, 3=info, 4=core, 5=debug, 6=debug detail)
     **/
    public NodeInfo logLevel(Integer logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Logging verbosity: 0-6 (0=silent, 1=error, 2=warn, 3=info, 4=core, 5=debug, 6=debug detail)")
    @JsonProperty("logLevel")
    public Integer getLogLevel() {
        return logLevel;
    }
    public void setLogLevel(Integer logLevel) {
        this.logLevel = logLevel;
    }



    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeInfo nodeInfo = (NodeInfo) o;
        return Objects.equals(this.identity, nodeInfo.identity) &&
                Objects.equals(this.committingTransactions, nodeInfo.committingTransactions) &&
                Objects.equals(this.networkId, nodeInfo.networkId) &&
                Objects.equals(this.logLevel, nodeInfo.logLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identity, committingTransactions, networkId, logLevel);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class NodeInfo {\n");

        sb.append("    identity: ").append(toIndentedString(identity)).append("\n");
        sb.append("    committingTransactions: ").append(toIndentedString(committingTransactions)).append("\n");
        sb.append("    networkId: ").append(toIndentedString(networkId)).append("\n");
        sb.append("    logLevel: ").append(toIndentedString(logLevel)).append("\n");
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

