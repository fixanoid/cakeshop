/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import com.jpmorgan.ib.caonpd.ethereum.enterprise.test.config.TestAppConfig;

import org.springframework.util.Assert;


/**
 *
 * @author N631539
 */

@ContextConfiguration(classes = TestAppConfig.class ,loader = AnnotationConfigContextLoader.class)
public class GethAdminHttpUnitTestRest extends AbstractTestNGSpringContextTests {

    
    public static final String MINER_URL="http://localhost:8080/ethereum-enterprise/miner/";
    public static final String NODE_URL="http://localhost:8080/ethereum-enterprise/node/";
    
    @Value("${geth.genesis}")
    private String genesis;
    
    //@Value("${geth.peer.addr}")
    private String peerAddress;
    
    private static JsonParser jsonParser;
    

    @BeforeTest
    public static void setEnv() {
        System.setProperty("eth.environment", "test");
        jsonParser = new JsonParser();
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty("eth.environment");
        jsonParser = null;
    }

    
    @Test(enabled = false)
    public void testNodeInfo() {
        String url = NODE_URL+AdminBean.ADMIN_NODE_INFO_KEY;
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        Assert.notNull(result);
        System.out.println("Geth func call Node Info :" + result);
        JsonObject jsonResult = jsonParser.parse(result).getAsJsonObject();
        Assert.notNull(jsonResult.get("result"));

    }


    @Test(enabled = false)
    public void testNodePeers() {
        String url = NODE_URL+AdminBean.ADMIN_PEERS_KEY;
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        Assert.notNull(result);
        System.out.println("Geth func call node peers :" + result);
        JsonObject jsonResult = jsonParser.parse(result).getAsJsonObject();
        Assert.notNull(jsonResult.get("result"));

    }
    
    @Test(enabled = false)

    public void testNodeVerbosity() {

        String url = NODE_URL+AdminBean.ADMIN_VERBOSITY_KEY;
        String funcArgs = "0";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        Assert.notNull(result);
        System.out.println("Geth func call node verbosity :" + result);
        JsonObject jsonResult = jsonParser.parse(result).getAsJsonObject();
        Assert.notNull(jsonResult.get("result"));
        Assert.isTrue(Boolean.parseBoolean(jsonResult.get("result").toString()));

    }

    @Test(enabled = false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testNodeDatadir() {

        String url = NODE_URL+AdminBean.ADMIN_DATADIR_KEY;
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        Assert.notNull(result);
        System.out.println("Geth func call node Datadir :" + result);
        JsonObject jsonResult = jsonParser.parse(result).getAsJsonObject();
        Assert.notNull(jsonResult.get("result"));

    }
  
   @Test(enabled = false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testMinerStart() {        

        String url = MINER_URL+AdminBean.ADMIN_MINER_START_KEY;
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        Assert.notNull(result);
        JsonObject jsonResult = jsonParser.parse(result).getAsJsonObject();
        Assert.isNull(jsonResult.get("error"));
        System.out.println("Miner func call miner start :" + result);

    }
    
    @Test(enabled = false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testMinerStop() {        

        String url = MINER_URL+AdminBean.ADMIN_MINER_STOP_KEY;
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        Assert.notNull(result);
        System.out.println("Miner func call miner stop :" + result);
        JsonObject jsonResult = jsonParser.parse(result).getAsJsonObject();
        Assert.notNull(jsonResult.get("result"));
        Assert.isTrue(Boolean.parseBoolean(jsonResult.get("result").toString()));
        
    }
    
    @Test(enabled=false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testMinerStatus() {        

        String url = MINER_URL+AdminBean.ADMIN_MINER_MINING_KEY;
        String funcArgs = " ";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        Assert.notNull(result);
        System.out.println("Miner func call miner status :" + result);
        JsonObject jsonResult = jsonParser.parse(result).getAsJsonObject();
        Assert.notNull(jsonResult.get("result"));
    }
    
     @Test(enabled = false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testAddPeer() {        
        Assert.notNull(peerAddress);
        String url = MINER_URL+AdminBean.ADMIN_ADD_PEER_KEY;
        //This has to be updated to have a valid peer
        String funcArgs = peerAddress;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        Assert.notNull(result);
        System.out.println("Geth func call add a peer :" + result);
        JsonObject jsonResult = jsonParser.parse(result).getAsJsonObject();
        Assert.notNull(jsonResult.get("result"));
        Assert.isTrue(Boolean.parseBoolean(jsonResult.get("result").toString()));
    }
    
     @Test(enabled = false)
    //To run test - comment out (enabled = false) from Test annotation
    public void testGetNodeInfo() {        
        String url = "http://localhost:8090/ethereum-enterprise/node/node-info";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        String result = restTemplate.postForObject(url, body, String.class);
        Assert.notNull(result);
        System.out.println("Geth func call get node info :" + result);
    }

}
