package com.jpmorgan.ib.caonpd.ethereum.enterprise.test.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testng.Assert.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jpmorgan.ib.caonpd.ethereum.enterprise.controller.NodeController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.testng.annotations.Test;


public class NodeControllerTest extends BaseControllerTest {

    @Autowired
    NodeController nodeController;

    private static JsonParser jsonParser;

    public NodeControllerTest() {
        super();
        jsonParser = new JsonParser();
    }

    @Override
    public Object getController() {
    	return nodeController;
    }

    @Test
    public void testInvalidEndPoint() throws Exception {
        mockMvc.perform(post("/api/node/testendpoint")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(""))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testNodeStatus() throws Exception {
        mockMvc.perform(post("/api/node/status")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(""))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    @Test
    public void testNodeGet() throws Exception {
        mockMvc.perform(post("/api/node/get")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(""))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testMinerStatus() throws Exception {
        assertNotNull(mockMvc);
        commonTest("","/api/node/miner/mining");
    }

    @Test
    public void testMinerStart() throws Exception {
        assertNotNull(mockMvc);
        commonTest("","/api/node/miner/start");
        commonTest("{\"args\":\"3\"}","/api/node/miner/start");
    }

    @Test
    public void testMinerStop() throws Exception {
        assertNotNull(mockMvc);
        commonTest("","/api/node/miner/stop");
    }

    private void commonTest(String body,String endPoint) throws Exception {
        MvcResult result = mockMvc.perform(post(endPoint)
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(body))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();
        String resultStr = result.getResponse().getContentAsString();
        JsonObject jsonResult = jsonParser.parse(resultStr).getAsJsonObject();
        assertNotNull(jsonResult.get("data"));
        assertNull(jsonResult.get("errors"));

    }

}
