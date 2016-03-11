package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;

import java.util.Map;

import org.testng.annotations.Test;

public class GethRpcTest extends BaseGethRpcTest {

    @Test
    public void testExecWithParams() throws APIException {
        String method = "eth_getBlockByNumber";

        Map<String, Object> data = geth.executeGethCall(method, new Object[]{ "latest", false });
        assertNotNull(data.get("hash"));


        String expectedHash = "0x4c5dc2805d90a33fa4e5358346d5335d4f6aeefd7e839952ef4e070e3a8412d2";

        data = geth.executeGethCall(method, new Object[]{ 0, false });
        assertEquals(data.get("hash"), expectedHash);

        data = geth.executeGethCall("eth_getBlockByHash", new Object[]{ expectedHash, false });
        assertEquals(data.get("hash"), expectedHash);
    }

}
