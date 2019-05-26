package com.moofMonkey.steam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class SteamBase {
	static class _HEX {
		public static String encode(byte[] ar) {
			StringBuilder sb = new StringBuilder(ar.length * 2);
			for(byte b: ar)
				sb.append(String.format("%02x", b));
			return sb.toString();
		}
	}

	public static class _HMACSHA1 {
		public static byte[] encode(byte[] key, byte[] value) {
			try {
				SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
				Mac mac = Mac.getInstance("HmacSHA1");
				
				mac.init(signingKey);
				
				return mac.doFinal(value);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static class _Base64 {
		public static byte[] FromBase64String(String s) throws Throwable {
			try {
				return Base64.getDecoder().decode(s);
			} catch (Exception ex) {
				throw new Throwable("Invalid Base64 string!", ex);
			}
		}

		public static String ToBase64String(byte[] b) {
			return new String(Base64.getEncoder().encode(b));
		}
	}
	
	public static final TimeCorrector tc = TimeCorrector.getInstance();

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
	
	public ArrayList<String> getResponse(String url, String cookies) throws Throwable {
		URLConnection uc = new URL(url).openConnection();
		uc.setRequestProperty("Cookie", cookies);
		uc.setRequestProperty("User-Agent", "Valve");
		uc.setRequestProperty("Accept-Language", "en-US");
		BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
		ArrayList<String> response = new ArrayList<String>();
		String line;

		while ((line = br.readLine()) != null)
			response.add(line);

		return response;
	}

	public static String extractStringValue(String JSON, String value) {
		char[] json = JSON.toCharArray();
		String tmp = "";
		boolean found = false;

		int index = JSON.indexOf(value);
		if(index < 0)
			return "";
		char prev = 0, element;
		for (int i = index + value.length() + 1; i < json.length; i++) {
			element = json[i];

			if (element == '"' && prev != '\\')
				if (found)
					break;
				else {
					found = true;
					continue;
				}

			if (found)
				tmp += element;
			prev = element;
		}

		return tmp;
	}
	
	public static String extractGSONRaw(String JSON, String value) {
		value = "\"" + value + "\"";
		char[] json = JSON.toCharArray();
		String tmp = "";

		for (int i = JSON.indexOf(value) + value.length() + 1; i < json.length; i++) {
			char element = json[i];

			if (element == ',')
				break;

			tmp += element;
		}

		return tmp;
	}

	public static String extractGSONStringValue(String JSON, String value) {
		String s = extractGSONRaw(JSON, value);
		return s.substring(1, s.length() - 1);
	}
	
	public static long extractGSONLongValue(String JSON, String value) {
		String s = extractGSONStringValue(JSON, value);
		if(s.length() == 0)
			return 0;
		return Long.parseLong(s);
	}
	
	public static boolean extractGSONBooleanValue(String JSON, String value) {
		String s = extractGSONRaw(JSON, value);
		if(s.length() == 0)
			return false;
		return Boolean.parseBoolean(s);
	}

	public static BigInteger extractBigIntegerValue(String JSON, String value) {
		String s = extractStringValue(JSON, value);
		if(s.length() == 0)
			return new BigInteger("0");
		return new BigInteger(s);
	}
	
	public static boolean extractBooleanValue(String JSON, String value) {
		String s = extractStringValue(JSON, value);
		if(s.length() == 0)
			return false;
		return Boolean.parseBoolean(s);
	}

	public static long extractLongValue(String JSON, String value) {
		String s = extractStringValue(JSON, value);
		if(s.length() == 0)
			return 0;
		return Long.parseLong(s);
	}
}
