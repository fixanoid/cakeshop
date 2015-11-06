/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.ib.caonpd.ethereum.enterperise.service.impl;

import com.jpmorgan.ib.caonpd.ethereum.enterperise.service.GethHttpService;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author I629630
 */

@Service
public class GethHttpServiceImpl implements GethHttpService {
    
    //TODO: make it readable from properties
    @Value("${geth.url}")
    private String url;

    //TODO: Method to add coinbase upon start of the app. @PostCreate
    @Override
    public String executeGethCall(String json) {
        RestTemplate restTemplate = new RestTemplate();        
        HttpHeaders headers = new HttpHeaders();       
        headers.setContentType(APPLICATION_JSON);
        HttpEntity <String> httpEntity = new HttpEntity(json, headers);
        ResponseEntity <String> response = restTemplate.exchange(url, POST, httpEntity, String.class);
        return response.getBody();        
    }    
}
