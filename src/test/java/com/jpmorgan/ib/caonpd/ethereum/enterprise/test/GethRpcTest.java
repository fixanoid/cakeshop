package com.jpmorgan.ib.caonpd.ethereum.enterprise.test;

import static org.testng.Assert.*;

import com.jpmorgan.ib.caonpd.ethereum.enterprise.error.APIException;

import java.math.BigInteger;
import java.util.Map;

import org.testng.annotations.Test;

public class GethRpcTest extends BaseGethRpcTest {

    @Test
    public void testExecWithParams() throws APIException {
        String method = "eth_getBlockByNumber";
        Object input = new BigInteger("0");
        System.out.println(input);



        String hash = "0x437d85e5a21ab984836930176ec31bd22a7b174b8a70a2d08c5173f554b7384b";

        Map<String, Object> data = geth.executeGethCall(method, new Object[]{ "latest", false });
        assertEquals(data.get("hash"), hash);

        data = geth.executeGethCall(method, new Object[]{ 0, false });
        assertEquals(data.get("hash"), hash);

        data = geth.executeGethCall("eth_getBlockByHash", new Object[]{ hash, false });
        assertEquals(data.get("hash"), hash);
    }

}
