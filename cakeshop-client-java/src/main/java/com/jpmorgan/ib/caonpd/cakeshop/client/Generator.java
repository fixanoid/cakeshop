package com.jpmorgan.ib.caonpd.cakeshop.client;

import com.google.common.escape.Escapers;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI;
import com.jpmorgan.ib.caonpd.cakeshop.model.ContractABI.Entry.Param;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType.AddressType;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType.BoolType;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType.Bytes32Type;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType.BytesType;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType.IntType;
import com.jpmorgan.ib.caonpd.cakeshop.model.SolidityType.StringType;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;


public class Generator {

    public static void main(String[] args) throws ParseException, IOException {

        Options options = new Options();
        options.addOption("f", "file", true, "File containing ABI JSON");
        options.addOption("p", "package", true, "Package name for generated code");
        options.addOption("c", "class", true, "Class name for generated code");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String json = null;
        if (cmd.hasOption("f")) {
            json = FileUtils.readFileToString(new File(cmd.getOptionValue("f")));
        }

        String packageName = null;
        if (cmd.hasOption("p")) {
            packageName = cmd.getOptionValue("p");
        }

        String className = null;
        if (cmd.hasOption("c")) {
            className = cmd.getOptionValue("c");
        }

        new Generator(className, packageName, json).generate();
    }

    private String className;
    private String packageName;
    private String jsonAbi;

    private ContractABI abi;

    private StringBuilder sb;

    public Generator(String className, String packageName, String jsonAbi) {
        this.sb = new StringBuilder();
        this.className = className;
        this.packageName = packageName;
        this.jsonAbi = jsonAbi;
    }

    public void generate() throws IOException {
        abi = ContractABI.fromJson(jsonAbi);

        VelocityEngine v = new VelocityEngine();
        v.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        v.setProperty("resource.loader", "class");
        v.init();

        Template t = v.getTemplate("contract.vm");
        VelocityContext context = new VelocityContext();
        context.put("util", new VUtil());
        context.put("className", className);
        context.put("packageName", packageName);
        context.put("jsonAbi", Escapers.builder().addEscape('"', "\\\"").build().escape(jsonAbi.trim()));
        context.put("abi", abi);

        StringWriter sw = new StringWriter();
        t.merge(context, sw);
        System.out.println(sw.toString());
    }

    public class VUtil {

        /**
         * Get the list of params as used in a Java method signature
         *
         * @param inputs
         * @return
         */
        public String methodSignature(List<Param> inputs) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < inputs.size(); i++) {
                Param param = inputs.get(i);
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(typeSignature(param));
            }
            return sb.toString();
        }

        /**
         * Get the list of input params, without types
         *
         * @param inputs
         * @return
         */
        public String inputList(List<Param> inputs) {
            // collect inputs
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < inputs.size(); i++) {
                Param param = inputs.get(i);
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(param.name);
            }
            return sb.toString();
        }

        /**
         * Get the type signature for a parameter
         *
         * e.g., "bytes[] foo" for "bytes32 foo"
         *
         * @param param
         * @return
         */
        public String typeSignature(Param param) {
            String s = getJavaTypeFor(param.type);
            if (isArrayType(param.type)) {
                s += "[]";
            }
            return s + " " + param.name;
        }

        public boolean isArrayType(SolidityType type) {
            return (
                (type.isDynamicType() && !(type instanceof StringType))
                || type instanceof Bytes32Type
                );
        }

        /**
         * Map a solidity type to the comparable java type
         *
         * @param type
         * @return
         */
        public String getJavaTypeFor(SolidityType type) {
            if (type instanceof AddressType) {
                return "String";
            } else if (type instanceof BoolType) {
                return "Boolean";
            } else if (type instanceof IntType) {
                return "BigInteger";
            } else if (type instanceof StringType) {
                return "String";
            } else if (type instanceof BytesType) {
                return "byte";
            } else if (type instanceof Bytes32Type) {
                return "byte";
            }
            return "Object ";
        }
    }

}
