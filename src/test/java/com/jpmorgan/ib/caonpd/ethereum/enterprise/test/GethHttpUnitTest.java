package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.junit.Assert.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.gson.Gson;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.model.RequestModel;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.service.GethHttpService;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.test.config.TestAppConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;

/**
 *
 * @author I629630
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {WebConfigTest.class})
@ContextConfiguration(classes = TestAppConfig.class ,loader = AnnotationConfigContextLoader.class)

public class GethHttpUnitTest extends AbstractTestNGSpringContextTests{

    @Autowired
    private GethHttpService service;

    @BeforeTest
    public static void setEnv() {
        System.setProperty("eth.environment", "test");
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty("eth.environment");
    }

    @Value("${geth.datadir}")
    private String datadir;


    //@Test(priority=0)
    public void testStart () {
        Boolean started = service.start();
        assertEquals(started, true);
        System.out.println("Server started :" + started);
    }

    //@Test(dependsOnMethods = "testStart")
    public void testService() throws APIException {
        String funcName = "admin_peers";
        String funcArgs = " ";
        RequestModel request = new RequestModel("2.0", funcName, funcArgs.split(","), "id");
        Gson gson = new Gson();
        String response = service.executeGethCall(gson.toJson(request));
        System.out.println("Geth func call response :" + response);
    }

    //@Test(dependsOnMethods = "testService")
    public void testStop() {
        Boolean stopped = service.stopGeth();
        assertEquals(stopped, true);
        System.out.println("Server stopped :" + stopped);
    }

    //@Test(dependsOnMethods = "testStop")
    public void testDeleteEthDatabase() {
        String eth_datadir = System.getProperty("user.home") + datadir + "-test";
        Boolean deleted = service.deleteEthDatabase(eth_datadir);
        assertEquals(deleted, true);
        System.out.println("Database deleted :" + deleted);
    }
}
