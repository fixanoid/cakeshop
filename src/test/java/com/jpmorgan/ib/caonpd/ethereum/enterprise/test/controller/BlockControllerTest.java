package com.jpmorgan.ib.caonpd.ethereum.enterprise.test.controller;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testng.Assert.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.controller.BlockController;


public class BlockControllerTest extends BaseControllerTest {

    @Autowired
    BlockController blockController;

    public BlockControllerTest() {
        super();
    }

    @Override
    public Object getController() {
    	return blockController;
    }

    @Test
    public void testGetBlockByNumber() throws Exception {
        assertNotNull(mockMvc);
        commonTest("{\"number\":0}");
    }

    @Test
    public void testGetBlockByHash() throws Exception {
        commonTest("{\"hash\":\"0xb067233bfb768b2d5b7c190b13601f5eb8628e8daf02bb21dd091369c330c25a\"}");
    }

    @Test
    public void testGetBlockByTag() throws Exception {
        commonTest("{\"tag\":\"latest\"}");
    }

    private void commonTest(String body) throws Exception {
        mockMvc.perform(post("/block/get")
                .contentType(MediaType.APPLICATION_JSON_VALUE).content(body))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(containsString("{\"number\":0")));

    }

}
