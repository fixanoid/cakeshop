package com.jpmorgan.ib.caonpd.cakeshop.client.model.req;

public class ContractMethodCallCommand {

    private String from;
    private String address;
    private String method;
    private Object[] args;
    private Object blockNumber;


    /**
     * Account to sign message with
     *
     * @param from String address
     * @return
     */
    public ContractMethodCallCommand from(String from) {
        this.from = from;
        return this;
    }

    /**
     * Contract ID (address)
     *
     * @param id
     * @return
     */
    public ContractMethodCallCommand address(String address) {
        this.address = address;
        return this;
    }

    /**
     * Method name to call
     *
     * @param method String method name
     * @return
     */
    public ContractMethodCallCommand method(String method) {
        this.method = method;
        return this;
    }

    /**
     * Array of method arguments
     *
     * @param args
     * @return
     */
    public ContractMethodCallCommand args(Object[] args) {
        this.args = args;
        return this;
    }

    /**
     * Read state as of the given block number (can be a number or tag; only applies to "read" method)
     *
     * @param blockNumber  String tag or Long number
     * @return
     */
    public ContractMethodCallCommand blockNumber(Object blockNumber) {
        this.blockNumber = blockNumber;
        return this;
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String id) {
        this.address = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Object getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Object blockNumber) {
        this.blockNumber = blockNumber;
    }

}
