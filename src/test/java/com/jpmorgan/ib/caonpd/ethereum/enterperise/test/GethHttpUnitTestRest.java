/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterperise.test;

import com.jpmorgan.ib.caonpd.ethereum.enterperise.config.WebConfigTest;

import static org.junit.Assert.assertEquals;
import org.springframework.http.HttpHeaders;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 *
 * @author I629630
 */

@ContextConfiguration(classes = WebConfigTest.class ,loader = AnnotationConfigContextLoader.class)

public class GethHttpUnitTestRest extends AbstractTestNGSpringContextTests {
    
    @BeforeTest
    public static void setEnv() {
        System.setProperty("eth.environment", "local");
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty("eth.environment");
    }
    
    @Test(enabled = false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testPost() {        
        //Change to whatever url your local tomcat  is set
        //Webapp must be up and running to execute this test!!!
        String url = "http://localhost:8090/ethereum-enterperise/submit_func";
        String funcName = "admin_peers";
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("func_name", funcName);
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        System.out.println("Geth func call response :" + result);

    }
    
    @Test(enabled = false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testStartRest() {
        //Change to whatever url your local tomcat  is set
        //Webapp must be up and running to execute this test!!!
        String url = "http://localhost:8090/ethereum-enterperise/start_geth";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        Boolean result = restTemplate.postForObject(url, null, Boolean.class);
        assertEquals(result, true);
        System.out.println("Server started :" + result);
    }
    
}
