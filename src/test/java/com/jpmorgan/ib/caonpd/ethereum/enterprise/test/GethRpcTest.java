package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;

import java.util.Map;

import org.testng.annotations.Test;

public class GethRpcTest extends BaseGethRpcTest {

    @Test
    public void testExecWithParams() throws APIException {
        String method = "eth_getBlockByNumber";
        String expectedHash = "0xd93b8da4c2f48c98e2cb76bef403ec22cada28331946218487b0fd1335e52bdd";

        Map<String, Object> data = geth.executeGethCall(method, new Object[]{ "latest", false });
        assertEquals(data.get("hash"), expectedHash);

        data = geth.executeGethCall(method, new Object[]{ 0, false });
        assertEquals(data.get("hash"), expectedHash);

        data = geth.executeGethCall("eth_getBlockByHash", new Object[]{ expectedHash, false });
        assertEquals(data.get("hash"), expectedHash);
    }

}
