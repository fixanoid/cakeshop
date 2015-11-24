package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.WebConfigTest;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;

@ContextConfiguration(classes = WebConfigTest.class ,loader = AnnotationConfigContextLoader.class)
public class GethRpcTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private GethHttpService service;

    @Value("${geth.genesis}")
    private String genesis;

    @Value("${geth.datadir}")
    private String ethDataDir;

    @BeforeClass
    public void startGeth() {
			String genesisDir =  System.getProperty("user.dir") + "/geth-resources/genesis/genesis_block.json";
			String command = System.getProperty("user.dir") + "/geth-resources/";
			String eth_datadir = System.getProperty("user.home") + ethDataDir + "-test";
			Boolean started = service.startGeth(command, genesisDir, eth_datadir, null);
    }

    @AfterClass
    public void stopGeth() {
    	service.stopGeth();
    }

    @BeforeTest
    public static void setEnv() {
        System.setProperty("eth.environment", "local");
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty("eth.environment");
    }

    @Test
    public void testExecWithParams() throws JsonParseException, JsonMappingException, IOException {
        String method = "eth_getBlockByNumber";
        Object input = new BigInteger("0");
        System.out.println(input);

        String hash = "0xb067233bfb768b2d5b7c190b13601f5eb8628e8daf02bb21dd091369c330c25a";
        Map<String, Object> data = service.executeGethCall(method, new Object[]{ "latest", false });
        assertEquals(data.get("hash"), hash);

        data = service.executeGethCall(method, new Object[]{ 0, false });
        assertEquals(data.get("hash"), hash);

        data = service.executeGethCall("eth_getBlockByHash", new Object[]{ hash, false });
        assertEquals(data.get("hash"), hash);
    }
}
