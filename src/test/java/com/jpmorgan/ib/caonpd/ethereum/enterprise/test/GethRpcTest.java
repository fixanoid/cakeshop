package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import java.math.BigInteger;
import java.util.Map;

import org.testng.annotations.Test;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;

public class GethRpcTest extends BaseGethRpcTest {

    @Test
    public void testExecWithParams() throws APIException {
        String method = "eth_getBlockByNumber";
        Object input = new BigInteger("0");
        System.out.println(input);

        String hash = "0xb067233bfb768b2d5b7c190b13601f5eb8628e8daf02bb21dd091369c330c25a";
        Map<String, Object> data = service.executeGethCall(method, new Object[]{ "latest", false });
        assertEquals(data.get("hash"), hash);

        data = service.executeGethCall(method, new Object[]{ 0, false });
        assertEquals(data.get("hash"), hash);

        data = service.executeGethCall("eth_getBlockByHash", new Object[]{ hash, false });
        assertEquals(data.get("hash"), hash);
    }

}
