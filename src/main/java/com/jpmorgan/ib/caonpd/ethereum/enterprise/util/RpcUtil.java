package com.jpmorgan.ib.caonpd.ethereum.enterprise.util;

import java.util.Map;

public class RpcUtil {

	public static Long toLong(String key, Map<String, Object> blockData) {
		String str = (String)blockData.get(key);
		if (str == null) {
			return null;
		}
		return Long.decode(str);
	}

}
