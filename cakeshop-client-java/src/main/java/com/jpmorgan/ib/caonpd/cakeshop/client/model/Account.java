package com.jpmorgan.ib.caonpd.cakeshop.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import io.swagger.annotations.ApiModelProperty;


@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-26T13:00:18.987-04:00")
public class Account   {

    private String address = null;
    private String balance = null;


    /**
     * 160-bit identifier
     **/
    public Account address(String address) {
        this.address = address;
        return this;
    }

    @ApiModelProperty(example = "null", value = "160-bit identifier")
    @JsonProperty("address")
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }


    /**
     * A scalar value equal to the number of Wei owned by this address.
     **/
    public Account balance(String balance) {
        this.balance = balance;
        return this;
    }

    @ApiModelProperty(example = "null", value = "A scalar value equal to the number of Wei owned by this address.")
    @JsonProperty("balance")
    public String getBalance() {
        return balance;
    }
    public void setBalance(String balance) {
        this.balance = balance;
    }



    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Account account = (Account) o;
        return Objects.equals(this.address, account.address) &&
                Objects.equals(this.balance, account.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, balance);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Account {\n");

        sb.append("    address: ").append(toIndentedString(address)).append("\n");
        sb.append("    balance: ").append(toIndentedString(balance)).append("\n");
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

