package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.ContractABI.Function;

import java.util.HashMap;
import java.util.Map;

public class TransactionRequest {

    public static final int DEFAULT_GAS = 3_141_590;

    private String fromAddress;

    private String contractAddress;

    private ContractABI abi;

    private Function function;

    private Map<String, Object> args;

    public TransactionRequest(String fromAddress, String contractAddress, ContractABI abi, String method, Object[] args) throws APIException {

        this.fromAddress = fromAddress;
        this.contractAddress = contractAddress;
        this.abi = abi;

	    this.function = abi.getFunction(method);
	    if (this.function == null) {
	        throw new APIException("Invalid method '" + method + "'");
	    }

	    this.args = new HashMap<>();
	    this.args.put("from", this.fromAddress);
	    this.args.put("to", this.contractAddress);
	    this.args.put("gas", DEFAULT_GAS);
	    this.args.put("data", this.function.encodeAsHex(args));

    }

    public Object[] getArgsArray() {
        return new Object[] { getArgs() };
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public ContractABI getAbi() {
        return abi;
    }

    public void setAbi(ContractABI abi) {
        this.abi = abi;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

}
