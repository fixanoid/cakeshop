package com.jpmorgan.ib.caonpd.ethereum.enterperise.test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.gson.Gson;
import com.jpmorgan.ib.caonpd.ethereum.enterperise.config.WebConfigTest;
import com.jpmorgan.ib.caonpd.ethereum.enterperise.model.RequestModel;
import com.jpmorgan.ib.caonpd.ethereum.enterperise.service.GethHttpService;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 *
 * @author I629630
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {WebConfigTest.class})
@ContextConfiguration(classes = WebConfigTest.class ,loader = AnnotationConfigContextLoader.class)

public class GethHttpUnitTest extends AbstractTestNGSpringContextTests{

    @Autowired
    private GethHttpService service;
    
    @BeforeTest
    public static void setEnv() {
        System.setProperty("eth.environment", "local");
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty("eth.environment");
    }

    @Test(priority=0)
    public void testStart () {
        String genesisDir =  System.getProperty("user.dir") + "/geth-resources/genesis/genesis_block.json";
        String command = System.getProperty("user.dir") + "/geth-resources/";
        Boolean started = service.startGeth(command, genesisDir);
        assertEquals(started, true);
        System.out.println("Server started :" + started);
    }  
    
    @Test(dependsOnMethods = "testStart")
    public void testService() {
        String funcName = "admin_peers";
        String funcArgs = " ";
        RequestModel request = new RequestModel("2.0", funcName, funcArgs.split(","), "id");
        Gson gson = new Gson();
        String response = service.executeGethCall(gson.toJson(request));
        System.out.println("Geth func call response :" + response);
    }
    
    @Test(dependsOnMethods = "testService")
    public void testStop() {
        Boolean stopped = service.stopGeth();
        assertEquals(stopped, true);
        System.out.println("Server stopped :" + stopped);
    }
    
    @Test(dependsOnMethods = "testStop")
    public void testDdeleteEthDatabase() {
        Boolean deleted = service.deletEthDatabase();
        assertEquals(deleted, true);
        System.out.println("Database deleted :" + deleted);
    }
}
