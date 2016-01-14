package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;

import java.util.Map;

import org.testng.annotations.Test;

public class GethRpcTest extends BaseGethRpcTest {

    @Test
    public void testExecWithParams() throws APIException {
        String method = "eth_getBlockByNumber";
        String expectedHash = "0x54321cd203712578849e9f697efbf7b05c2a72e3281d036677c7196b2996813d";

        Map<String, Object> data = geth.executeGethCall(method, new Object[]{ "latest", false });
        assertEquals(data.get("hash"), expectedHash);

        data = geth.executeGethCall(method, new Object[]{ 0, false });
        assertEquals(data.get("hash"), expectedHash);

        data = geth.executeGethCall("eth_getBlockByHash", new Object[]{ expectedHash, false });
        assertEquals(data.get("hash"), expectedHash);
    }

}
