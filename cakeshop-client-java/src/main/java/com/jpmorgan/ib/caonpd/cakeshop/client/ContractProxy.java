package com.jpmorgan.ib.caonpd.cakeshop.client;

import com.jpmorgan.ib.caonpd.cakeshop.client.api.ContractApi;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI;

import org.apache.commons.codec.binary.Base64;

public abstract class ContractProxy<T extends ContractProxy<T>> {

    protected ContractApi contractApi;

    protected String contractAddress;

    protected ContractProxy(ContractApi contractApi, String contractAddress) {
        this.contractApi = contractApi;
        this.contractAddress = contractAddress;
    }

    public abstract ContractABI getABI();

    public void processInputArgs(Object[] args) {
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];

            // base64 encode byte types
            if (arg instanceof Byte) {
                if (arg.getClass().isArray()) {
                    args[i] = Base64.encodeBase64String((byte[]) arg);
                } else {
                    args[i] = Base64.encodeBase64String(new byte[]{ (byte) arg });
                }
            }

        }
    }

    public Object[] processOutputArgs(Object[] args) {
        return args;
    }

    public T contractApi(ContractApi contractApi) {
        this.contractApi = contractApi;
        return (T) this;
    }

    public ContractApi getContractApi() {
        return contractApi;
    }

    public void setContractApi(ContractApi contractApi) {
        this.contractApi = contractApi;
    }

    public T contractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
        return (T) this;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

}
