package com.jpmorgan.ib.caonpd.ethereum.enterprise.model;

import static java.lang.String.*;
import static org.apache.commons.lang3.ArrayUtils.*;
import static org.apache.commons.lang3.StringUtils.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.SolidityType.IntType;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.util.RpcUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.bouncycastle.util.encoders.Hex;

public class ContractABI {

    public static class Param {
        public String name;
        public SolidityType type;
    }

    enum FunctionType {
        constructor,
        function
    }

    public static class Function {
        public boolean constant;
        public String name;
        public Param[] inputs;
        public Param[] outputs;
        public FunctionType type;

        private Function() {}


        public String encodeAsHex(Object ... args) {
            return Hex.toHexString(encode(args));
        }

        public byte[] encode(Object ... args) {
            return RpcUtil.merge(encodeSignature(), encodeArguments(args));
        }

        public byte[] encodeArguments(Object ... args) {

            if (args == null || args.length == 0) {
                if (inputs.length > 0) {
                    throw new RuntimeException("Too few arguments: 0 < " + inputs.length);
                }
                return new byte[0];
            }

            if (args.length > inputs.length) {
                throw new RuntimeException("Too many arguments: " + args.length + " > " + inputs.length);
            } else if (args.length < inputs.length) {
                throw new RuntimeException("Too few arguments: " + args.length + " < " + inputs.length);
            }

            int staticSize = 0;
            int dynamicCnt = 0;
            // calculating static size and number of dynamic params
            for (int i = 0; i < args.length; i++) {
                Param param = inputs[i];
                if (param.type.isDynamicType()) {
                    dynamicCnt++;
                }
                staticSize += param.type.getFixedSize();
            }

            byte[][] bb = new byte[args.length + dynamicCnt][];

            int curDynamicPtr = staticSize;
            int curDynamicCnt = 0;
            for (int i = 0; i < args.length; i++) {
                if (inputs[i].type.isDynamicType()) {
                    byte[] dynBB = inputs[i].type.encode(args[i]);
                    bb[i] = IntType.encodeInt(curDynamicPtr);
                    bb[args.length + curDynamicCnt] = dynBB;
                    curDynamicCnt++;
                    curDynamicPtr += dynBB.length;
                } else {
                    bb[i] = inputs[i].type.encode(args[i]);
                }
            }
            return RpcUtil.merge(bb);
        }

        private Object[] decode(byte[] encoded, Param[] params) {
            Object[] ret = new Object[params.length];

            int off = 0;
            for (int i = 0; i < params.length; i++) {
                Param param = params[i];
                if (params[i].type.isDynamicType()) {
                    ret[i] = param.type.decode(encoded, IntType.decodeInt(encoded, off).intValue());
                } else {
                    ret[i] = param.type.decode(encoded, off);
                }
                off += param.type.getFixedSize();
            }
            return ret;
        }

        public Object[] decode(byte[] encoded) {
            return decode(subarray(encoded, 4, encoded.length), inputs);
        }

        public Object[] decodeHexResult(String encodedRet) {
            // TODO validate input
            return decodeResult(Hex.decode(encodedRet.substring(2))); // chop off the leading 0x
        }

        public Object[] decodeResult(byte[] encodedRet) {
            return decode(encodedRet, outputs);
        }

        public String formatSignature() {
            StringBuilder paramsTypes = new StringBuilder();
            for (Param param : inputs) {
                paramsTypes.append(param.type.getCanonicalName()).append(",");
            }

            return format("%s(%s)", name, stripEnd(paramsTypes.toString(), ","));
        }

        public byte[] encodeSignature() {
            String signature = formatSignature();
            byte[] sha3Fingerprint = RpcUtil.sha3(signature);

            return Arrays.copyOfRange(sha3Fingerprint, 0, 4);
        }

        @Override
        public String toString() {
            return formatSignature();
        }

        public static Function fromJsonInterface(String json) {
            try {
                return new ObjectMapper().readValue(json, Function.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static Function fromSignature(String funcName, String ... paramTypes) {
            Function ret = new Function();
            ret.name = funcName;
            ret.constant = false;
            ret.type = FunctionType.function;
            ret.outputs = new Param[0];
            ret.inputs = new Param[paramTypes.length];
            for (int i = 0; i < paramTypes.length; i++) {
                ret.inputs[i] = new Param();
                ret.inputs[i].name = "param" + i;
                ret.inputs[i].type = SolidityType.getType(paramTypes[i]);
            }
            return ret;
        }
    }

    private Function[] functions;
    private Map<String, Function> functionMap;

    public ContractABI(Function[] functions) {
        this.functions = functions;
        functionMap = new HashMap<>();
        for (Function f : functions) {
            functionMap.put(f.name, f);
        }
    }

    public ContractABI(String jsonABI) throws IOException {
        this(new ObjectMapper().readValue(jsonABI, Function[].class));
    }

    public ContractABI(InputStream input) throws IOException {
        this(new ObjectMapper().readValue(input, Function[].class));
    }

    public Function getFunction(String name) {
        return functionMap.get(name);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
