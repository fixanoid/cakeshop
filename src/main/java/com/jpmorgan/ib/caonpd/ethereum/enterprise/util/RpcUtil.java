package com.jpmorgan.ib.caonpd.ethereum.enterprise.util;

import java.util.Map;

public class RpcUtil {

    public static Long toLong(String key, Map<String, Object> blockData) {
        return Long.decode((String)blockData.get(key));
    }

}
