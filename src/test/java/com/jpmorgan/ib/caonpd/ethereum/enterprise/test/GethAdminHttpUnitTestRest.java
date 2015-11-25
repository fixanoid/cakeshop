/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.springframework.http.MediaType.*;

import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


import org.springframework.beans.factory.annotation.Value;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.bean.AdminBean;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.config.TestWebConfig;


/**
 *
 * @author N631539
 */

@ContextConfiguration(classes = TestWebConfig.class ,loader = AnnotationConfigContextLoader.class)
public class GethAdminHttpUnitTestRest extends AbstractTestNGSpringContextTests {

    
    public static final String MINER_URL="http://localhost:8080/ethereum-enterprise/miner/";
    public static final String NODE_URL="http://localhost:8080/ethereum-enterprise/node/";
    
    @Value("${geth.genesis}")
    private String genesis;
    

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
    public void testNodeInfo() {
        //Change to whatever url your local tomcat  is set
        //Webapp must be up and running to execute this test!!!
        String url = NODE_URL+AdminBean.ADMIN_NODE_INFO_KEY;
        //String funcName = "admin_peers";
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        //body.add("func_name", funcName);
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        System.out.println("Geth func call response :" + result);

    }


    @Test(enabled = false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testNodePeers() {
        //Change to whatever url your local tomcat  is set
        //Webapp must be up and running to execute this test!!!
        String url = NODE_URL+AdminBean.ADMIN_PEERS_KEY;
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        //body.add("func_name", funcName);
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        System.out.println("Geth func call response :" + result);

    }
    
    @Test(enabled = false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testNodeVerbosity() {
        //Change to whatever url your local tomcat  is set
        //Webapp must be up and running to execute this test!!!
        String url = NODE_URL+AdminBean.ADMIN_VERBOSITY_KEY;
        String funcArgs = "0";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        //body.add("func_name", funcName);
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        System.out.println("Geth func call response :" + result);

    }

    @Test(enabled = false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testNodeDatadir() {
        //Change to whatever url your local tomcat  is set
        //Webapp must be up and running to execute this test!!!
        String url = NODE_URL+AdminBean.ADMIN_DATADIR_KEY;
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        //body.add("func_name", funcName);
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        System.out.println("Geth func call response :" + result);

    }
  
   @Test(enabled = true)
    //To run test - comment out (enabled = false) from Test annotation
    public void testMinerStart() {        
        //Change to whatever url your local tomcat  is set
        //Webapp must be up and running to execute this test!!!
        String url = MINER_URL+AdminBean.ADMIN_MINER_START_KEY;
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        //body.add("func_name", funcName);
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        System.out.println("Miner func call response :" + result);

    }
    
    @Test(enabled = false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testMinerStop() {        
        //Change to whatever url your local tomcat  is set
        //Webapp must be up and running to execute this test!!!
        String url = MINER_URL+AdminBean.ADMIN_MINER_STOP_KEY;
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        //body.add("func_name", funcName);
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        System.out.println("Miner func call response :" + result);
    }
    
    @Test(enabled = false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testMinerStatus() {        
        //Change to whatever url your local tomcat  is set
        //Webapp must be up and running to execute this test!!!
        String url = MINER_URL+AdminBean.ADMIN_MINER_MINING_KEY;
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        //body.add("func_name", funcName);
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        System.out.println("Miner func call response :" + result);
    }

}
