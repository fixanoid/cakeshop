package com.jpmorgan.ib.caonpd.cakeshop.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-26T17:07:52.216-04:00")
public class Event   {

    private String id = null;
    private String blockId = null;
    private Long blockNumber = null;
    private Long logIndex = null;
    private String transactionId = null;
    private String contractId = null;
    private String name = null;
    private Object data = null;


    /**
     **/
    public Event id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    /**
     **/
    public Event blockId(String blockId) {
        this.blockId = blockId;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("blockId")
    public String getBlockId() {
        return blockId;
    }
    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }


    /**
     **/
    public Event blockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("blockNumber")
    public Long getBlockNumber() {
        return blockNumber;
    }
    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }


    /**
     **/
    public Event logIndex(Long logIndex) {
        this.logIndex = logIndex;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("logIndex")
    public Long getLogIndex() {
        return logIndex;
    }
    public void setLogIndex(Long logIndex) {
        this.logIndex = logIndex;
    }


    /**
     **/
    public Event transactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("transactionId")
    public String getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }


    /**
     **/
    public Event contractId(String contractId) {
        this.contractId = contractId;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("contractId")
    public String getContractId() {
        return contractId;
    }
    public void setContractId(String contractId) {
        this.contractId = contractId;
    }


    /**
     **/
    public Event name(String name) {
        this.name = name;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    /**
     **/
    public Event data(Object data) {
        this.data = data;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("data")
    public Object getData() {
        return data;
    }
    public void setData(Object data) {
        this.data = data;
    }



    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Event event = (Event) o;
        return Objects.equals(this.id, event.id) &&
                Objects.equals(this.blockId, event.blockId) &&
                Objects.equals(this.blockNumber, event.blockNumber) &&
                Objects.equals(this.logIndex, event.logIndex) &&
                Objects.equals(this.transactionId, event.transactionId) &&
                Objects.equals(this.contractId, event.contractId) &&
                Objects.equals(this.name, event.name) &&
                Objects.equals(this.data, event.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, blockId, blockNumber, logIndex, transactionId, contractId, name, data);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Event {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    blockId: ").append(toIndentedString(blockId)).append("\n");
        sb.append("    blockNumber: ").append(toIndentedString(blockNumber)).append("\n");
        sb.append("    logIndex: ").append(toIndentedString(logIndex)).append("\n");
        sb.append("    transactionId: ").append(toIndentedString(transactionId)).append("\n");
        sb.append("    contractId: ").append(toIndentedString(contractId)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    data: ").append(toIndentedString(data)).append("\n");
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

