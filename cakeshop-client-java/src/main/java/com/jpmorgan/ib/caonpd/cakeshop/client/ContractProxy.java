package com.jpmorgan.ib.caonpd.cakeshop.client;

import com.jpmorgan.ib.caonpd.cakeshop.client.api.ContractApi;
import com.jpmorgan.ib.caonpd.cakeshop.client.model.req.ContractMethodCallCommand;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI.Entry.Param;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType.ArrayType;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType.Bytes32Type;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType.BytesType;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

public abstract class ContractProxy<T extends ContractProxy<T>> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ContractProxy.class);

    protected ContractApi contractApi;

    protected String contractAddress;

    protected ContractProxy(ContractApi contractApi, String contractAddress) {
        this.contractApi = contractApi;
        this.contractAddress = contractAddress;
    }

    public abstract ContractABI getABI();

    public ContractMethodCallCommand newCall(String method, Object[] args) {
        return new ContractMethodCallCommand().address(contractAddress).method(method).args(args);
    }

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

    public List<Object> processOutputArgs(List<Object> returnVals, List<Param> outputs) {
        return processOutputArgs(returnVals, outputs, false);
    }

    public List<Object> processOutputArgs(List<Object> returnVals, List<Param> outputs, boolean treatBytesAsStrings) {

        if (outputs == null || outputs.isEmpty()) {
            return returnVals;
        }

        List<Object> results = new ArrayList<>();

        for (int i = 0; i < returnVals.size(); i++) {
            Object ret = returnVals.get(i);
            Param out = outputs.get(i);

            SolidityType type = out.type;
            if (type instanceof ArrayType) {
                type = ((ArrayType) out.type).elementType; // use inner type
            }

            if (type instanceof BytesType || type instanceof Bytes32Type) {
                if (ret instanceof Byte) {
                    // single value
                    results.add(decodeBytes((String) ret, treatBytesAsStrings));

                } else if (ret instanceof List) {
                    // convert array of bytes
                    List<Object> argList = new ArrayList<>();
                    for (Object arg : (List) ret) {
                        argList.add(decodeBytes((String) arg, treatBytesAsStrings));
                    }
                    results.add(argList);

                } else {
                    LOG.warn("Not sure how to handle " + ret.getClass());
                    results.add(ret);
                }

            } else {
                results.add(ret);
            }
        }

        return results;
    }

    private Object decodeBytes(String str, boolean treatBytesAsStrings) {
        byte[] decoded = Base64.decodeBase64(str);
        if (treatBytesAsStrings) {
            return new String(decoded);
        } else {
            return decoded;
        }
    }

    public List<String> convertBytesToStrings(List<Object> byteList) {
        List<String> strings = new ArrayList<>();
        for (Object obj : byteList) {
            strings.add(new String((byte[]) obj));
        }
        return strings;
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
