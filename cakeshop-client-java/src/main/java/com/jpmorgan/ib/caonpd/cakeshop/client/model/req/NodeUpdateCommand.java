package com.jpmorgan.ib.caonpd.cakeshop.client.model.req;

public class NodeUpdateCommand {

    private String logLevel;
    private String networkId;
    private Boolean committingTransactions;
    private String extraParams;
    private String genesisBlock;

    public NodeUpdateCommand() {
    }

    public String getLogLevel() {
        return logLevel;
    }
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
    public String getNetworkId() {
        return networkId;
    }
    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }
    public Boolean getCommittingTransactions() {
        return committingTransactions;
    }
    public void setCommittingTransactions(Boolean committingTransactions) {
        this.committingTransactions = committingTransactions;
    }
    public String getExtraParams() {
        return extraParams;
    }
    public void setExtraParams(String extraParams) {
        this.extraParams = extraParams;
    }
    public String getGenesisBlock() {
        return genesisBlock;
    }
    public void setGenesisBlock(String genesisBlock) {
        this.genesisBlock = genesisBlock;
    }

}
