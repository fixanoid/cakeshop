package com.jpmorgan.ib.caonpd.ethereum.enterperise.test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.jpmorgan.ib.caonpd.ethereum.enterperise.config.WebConfigTest;
import org.junit.Ignore;

import org.junit.Test;
import org.junit.runner.RunWith;
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
@ContextConfiguration(classes = { WebConfigTest.class })
public class GethHttpUnitTest {


    @Test
    @Ignore
    public void test() {
        //Change to whatever url your local tomcat  is set
        String url = "http://localhost:8090/ethereum-enterperise/submit_func";        
        String funcName = "admin_peers";
        String funcArgs  = " ";
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();     
        body.add("func_name", funcName);
        body.add("func_args", funcArgs);
        String result = restTemplate.postForObject(url, body, String.class);
        System.out.println("Geth func call response :" + result);

    }    
    
}
