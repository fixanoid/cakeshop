package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.TestWebConfig;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;

@Test
@ContextConfiguration(classes = {TestWebConfig.class})
public abstract class BaseGethRpcTest extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(BaseGethRpcTest.class);

    static {
        System.setProperty("eth.environment", "test");
    }

    @Autowired
    private GethHttpService service;

    @Value("${geth.genesis}")
    private String genesis;

    @Value("${geth.datadir}")
    private String ethDataDir;

    @BeforeClass
    public void startGeth() {
    		LOG.debug("Starting Ethereum at test startup");
        String genesisDir =  System.getProperty("user.dir") + "/geth-resources/genesis/genesis_block.json";
        String command = System.getProperty("user.dir") + "/geth-resources/";
        String eth_datadir = System.getProperty("user.home") + ethDataDir;
        new File(eth_datadir).mkdirs();
        Boolean started = service.startGeth(command, genesisDir, eth_datadir, null);
        assertTrue(started);
    }

    @AfterClass
    public void stopGeth() {
    	LOG.debug("Stopping Ethereum at test teardown");
    	service.stopGeth();
			String eth_datadir = System.getProperty("user.home") + ethDataDir + "-test";
			try {
				FileUtils.deleteDirectory(new File(eth_datadir));
			} catch (IOException e) {
				logger.warn(e);
			}
    }


}
