package com.jpmorgan.ib.caonpd.cakeshop.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.cakeshop.error.APIException;
import com.jpmorgan.ib.caonpd.cakeshop.service.BlockService;
import com.jpmorgan.ib.caonpd.cakeshop.service.GethHttpService;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class GethHttpServiceTest extends BaseGethRpcTest {

    @Autowired
    private GethHttpService geth;

    @Autowired
    private BlockService blockService;

    @Test
    public void testReset() throws APIException {
        assertTrue(geth.isRunning());

        String blockId = blockService.get(null, 1L, null).getId();

        assertTrue(geth.reset());
        assertTrue(geth.isRunning());

        assertNotEquals(blockService.get(null, 1L, null).getId(), blockId);
    }

}
