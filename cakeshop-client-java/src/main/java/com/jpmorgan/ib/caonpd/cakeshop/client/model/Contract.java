package com.jpmorgan.ib.caonpd.cakeshop.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-26T13:00:18.987-04:00")
public class Contract   {

    private String id = null;
    private String author = null;
    private List<String> keys = new ArrayList<String>();
    private String abi = null;
    private String code = null;


    public enum CodeTypeEnum {
        SOLIDITY("solidity");

        private String value;

        CodeTypeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    private CodeTypeEnum codeType = CodeTypeEnum.SOLIDITY;


    /**
     * Contract unique identifier. In Ethereum this is the contract address.
     **/
    public Contract id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Contract unique identifier. In Ethereum this is the contract address.")
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    /**
     * Pubkey of sender on original create.
     **/
    public Contract author(String author) {
        this.author = author;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Pubkey of sender on original create.")
    @JsonProperty("author")
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }


    /**
     * Pubkeys that can access contract.
     **/
    public Contract keys(List<String> keys) {
        this.keys = keys;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Pubkeys that can access contract.")
    @JsonProperty("keys")
    public List<String> getKeys() {
        return keys;
    }
    public void setKeys(List<String> keys) {
        this.keys = keys;
    }


    /**
     * Contract ABI
     **/
    public Contract abi(String abi) {
        this.abi = abi;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Contract ABI")
    @JsonProperty("abi")
    public String getAbi() {
        return abi;
    }
    public void setAbi(String abi) {
        this.abi = abi;
    }


    /**
     * Contract source code
     **/
    public Contract code(String code) {
        this.code = code;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Contract source code")
    @JsonProperty("code")
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }


    /**
     * Type of code being submitted
     **/
    public Contract codeType(CodeTypeEnum codeType) {
        this.codeType = codeType;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Type of code being submitted")
    @JsonProperty("code_type")
    public CodeTypeEnum getCodeType() {
        return codeType;
    }
    public void setCodeType(CodeTypeEnum codeType) {
        this.codeType = codeType;
    }



    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Contract contract = (Contract) o;
        return Objects.equals(this.id, contract.id) &&
                Objects.equals(this.author, contract.author) &&
                Objects.equals(this.keys, contract.keys) &&
                Objects.equals(this.abi, contract.abi) &&
                Objects.equals(this.code, contract.code) &&
                Objects.equals(this.codeType, contract.codeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, author, keys, abi, code, codeType);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}

