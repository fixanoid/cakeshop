package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.testng.Assert.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.JsonMethodArgumentResolver;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.TestWebConfig;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.controller.BlockController;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;


@ContextConfiguration(classes = {TestWebConfig.class})
@WebAppConfiguration
public class BlockControllerTest extends AbstractTestNGSpringContextTests {

    static {
        System.setProperty("eth.environment", "local");
    }


    @Autowired
    private GethHttpService service;

    @Value("${geth.genesis}")
    private String genesis;

    @Value("${geth.datadir}")
    private String ethDataDir;


    private MockMvc mockMvc;

    @Autowired
    BlockController blockController;

    public BlockControllerTest() {
        super();
    }

    @BeforeClass
    public void startGeth() {
        String genesisDir =  System.getProperty("user.dir") + "/geth-resources/genesis/genesis_block.json";
        String command = System.getProperty("user.dir") + "/geth-resources/";
        String eth_datadir = System.getProperty("user.home") + ethDataDir + "-test";
        Boolean started = service.startGeth(command, genesisDir, eth_datadir, null);
        assertTrue(started);
    }

    @AfterClass
    public void stopGeth() {
    	service.stopGeth();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        mockMvc = standaloneSetup(blockController).setCustomArgumentResolvers(new JsonMethodArgumentResolver()).build();
    }

    @Test
    public void testGetBlockByNumber() throws Exception {
       assertNotNull(mockMvc);

       String body = "{\"number\":0}";
       System.out.println(body);
        mockMvc.perform(post("/block/get")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(body))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(containsString("{\"number\":0")));

    }

}
