package com.jpmorgan.ib.caonpd.ethereum.enterprise.util;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;
import org.bouncycastle.util.encoders.Hex;

public class RpcUtil {

    public static final int SHA3_DEFAULT_SIZE = 256;

	public static Long toLong(String key, Map<String, Object> blockData) {
		String str = (String)blockData.get(key);
		if (str == null) {
			return null;
		}
		return Long.decode(str);
	}

	public static byte[] sha3(String input) {
	    return sha3(input.getBytes());
	}

	public static byte[] sha3(byte[] input) {
        return new DigestSHA3(SHA3_DEFAULT_SIZE).digest(input);
	}

	/**
	 * Merge the given arrays into a single array
	 *
	 * @param arrays - arrays to merge
	 * @return - merged array
	 */
	public static byte[] merge(byte[]... arrays) {
	    int count = 0;
	    for (byte[] array: arrays) {
	        count += array.length;
	    }

	    // Create new array and copy all array contents
	    byte[] mergedArray = new byte[count];
	    int start = 0;
	    for (byte[] array: arrays) {
	        System.arraycopy(array, 0, mergedArray, start, array.length);
	        start += array.length;
	    }
	    return mergedArray;
	}

	public static String addrToHex(BigInteger addr) {
	    String hex = Hex.toHexString(addr.toByteArray());
	    if (hex.length() > 40) {
	        hex = hex.substring(hex.length()-40); // remove any leading 0 padding
	    }
	    return "0x" + hex;
	}

	@SuppressWarnings("rawtypes")
    public static String toString(Object object) {
	    if (object instanceof List) {
	        object = ((List) object).toArray();
	    }
	    return ToStringBuilder.reflectionToString(object, ShortToStringStyle.INSTANCE);
	}

	public static void puts(Object object) {
	    String str = (String) (object instanceof String ? object : toString(object));
	    System.out.println(str);
	}

	public static BigInteger hexToBigInteger(String hex) {
	    if (hex.startsWith("0x")) {
	        return new BigInteger(hex.substring(2), 16);
	    } else {
	        return new BigInteger(hex, 16);
	    }
	}

}
