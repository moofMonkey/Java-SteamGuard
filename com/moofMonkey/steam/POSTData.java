package com.moofMonkey.steam;

import java.util.HashMap;

public class POSTData extends HashMap<String, String> {
	private static final long serialVersionUID = 1L;

	public static String encodeUrlUnsafeChars(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '+' || c == '/' || c == '=')
				sb.append(String.format("%%%02x", new Object[] { Integer.valueOf(c) }));
			else
				sb.append(c);
		}
		return sb.toString();
	}
	
	@Override
	public String toString() {
		String toRet = "";
		for(Entry<String, String> e  : entrySet())
			toRet +=
					encodeUrlUnsafeChars (
						e.getKey()
					)
					+ "="
					+ encodeUrlUnsafeChars (
						e.getValue()
					)
					+ "&"
			;
		if(toRet.endsWith("&"))
			toRet = toRet.substring(0, toRet.length() - 1);
		return toRet;
	}
}
