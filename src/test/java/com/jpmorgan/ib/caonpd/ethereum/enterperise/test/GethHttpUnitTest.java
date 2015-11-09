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
import org.junit.BeforeClass;
import org.junit.Ignore;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author I629630
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {WebConfigTest.class})
public class GethHttpUnitTest {

    @Autowired
    private GethHttpService service;

    @BeforeClass
    public static void setEnv() {
        System.setProperty("eth.environment", "local");
    }

    @AfterClass
    public static void afterClass() {
        System.clearProperty("eth.environment");
    }

    @Test
    @Ignore
    public void testPost() {
        //Change to whatever url your local tomcat  is set
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

    @Test
    @Ignore
    public void testService() {
        String funcName = "admin_peers";
        String funcArgs = " ";
        RequestModel request = new RequestModel("2.0", funcName, funcArgs.split(","), "id");
        Gson gson = new Gson();
        String response = service.executeGethCall(gson.toJson(request));
        System.out.println("Geth func call response :" + response);
    }

}
