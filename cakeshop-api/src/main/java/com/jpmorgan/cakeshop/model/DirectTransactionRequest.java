package com.jpmorgan.cakeshop.model;

import com.jpmorgan.cakeshop.error.APIException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DirectTransactionRequest {

    public static final String BLOCK_LATEST = "latest";

    public static final int DEFAULT_GAS = 10_000_000;

    private String fromAddress;

    private String toAddress;

    private final String data;

    private List<String> geminiTo;

    private Object blockNumber;

    private final boolean isRead;

    public DirectTransactionRequest(String fromAddress, String toAddress, String data, boolean isRead) throws APIException {
        this(fromAddress, toAddress, data, isRead, null);
    }

    public DirectTransactionRequest(String fromAddress, String toAddress, String data, boolean isRead, Object blockNumber) throws APIException {
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.isRead = isRead;
        this.blockNumber = blockNumber;
        this.data = data;
    }

    public Object[] toGethArgs() {

        Map<String, Object> req = new HashMap<>();
	    req.put("from", fromAddress);
	    req.put("to", toAddress);
	    req.put("gas", DEFAULT_GAS);
        req.put("data", data);

	    if (geminiTo != null && !geminiTo.isEmpty()) {
            req.put("geminiTo", geminiTo);
	    }

        if (isRead) {
            if (blockNumber == null) {
                return new Object[] { req, BLOCK_LATEST };
            } else {
                return new Object[] { req, blockNumber };
            }
        } else {
            return new Object[] { req };
        }
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getContractAddress() {
        return toAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.toAddress = contractAddress;
    }

    public Object getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Object blockNumber) {
        this.blockNumber = blockNumber;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public List<String> getGeminiTo() {
        return geminiTo;
    }

    public void setGeminiTo(List<String> geminiTo) {
        this.geminiTo = geminiTo;
    }

}
