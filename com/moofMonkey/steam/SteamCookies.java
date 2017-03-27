package com.moofMonkey.steam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;

import javax.crypto.Cipher;
import javax.net.ssl.HttpsURLConnection;

/**
 * Big hint: https://github.com/DoctorMcKay/node-steamcommunity/blob/acbd71666bac0f6c2855e177ce02b3f73d324686/index.js#L106
 * @author moofmonkey
 *
 */
public class SteamCookies extends SteamBase {
	/**
	 * @param props
	 * @return 0 = cookies, 1 = response
	 * @throws Throwable
	 */
	public static Object[] getData(Properties props) throws Throwable {
		Object[] data;
		try {
			data = getData(props, -1, "");;
		} catch(CaptchaException e) {
			String url = e.captchaURL;
			System.out.println("Please enter captcha: " + url);
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			data = getData(props, e.captchaGID, br.readLine());
		}
		
		return data;
	}
	
	/**
	 * @param props
	 * @param captchagid
	 * @param captcha_textprops
	 * @return 0 = cookies, 1 = response
	 * @throws Throwable
	 */
	public static Object[] getData(Properties props, long captchagid, String captcha_text) throws Throwable {
		Cipher rsa = Cipher.getInstance("RSA");
		Object[] rsaKey = getRSAKey(props.username);
		rsa.init(Cipher.ENCRYPT_MODE, (PublicKey) rsaKey[0]);
		String passwordEnc = _Base64.ToBase64String(rsa.doFinal(props.password.getBytes("ASCII")));
		POSTData postData = new POSTData();
		
		postData.put("emailauth", "");
		postData.put("captchagid", Long.toString(captchagid));
		postData.put("captcha_text", captcha_text);
		postData.put("emailsteamid", "");
		postData.put("username", props.username);
		postData.put("password", passwordEnc);
		postData.put("twofactorcode", new SteamCodeGenerator(props).generateSteamGuardCode());
		postData.put("remember_login", Boolean.toString(true));
		postData.put("rsatimestamp", Long.toString((long) rsaKey[1]));
		postData.put("oauth_client_id", "DE45CD61");
		postData.put("rsatimestamp", Long.toString((long) rsaKey[1]));
		postData.put("oauth_scope", "read_profile write_profile read_client write_client");
		postData.put("loginfriendlyname", "#login_emailauth_friendlyname_mobile");
		postData.put("donotcache", Long.toString(System.currentTimeMillis()));
		
		String postStr = postData.toString();
		
		HttpsURLConnection con = (HttpsURLConnection) new URL("https://steamcommunity.com/login/dologin/").openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Length", Integer.toString(postStr.length()));
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
		con.connect();
		bw.write(postStr);
		bw.flush();
		
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} catch(Throwable t) {
			br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			System.err.println(t.getMessage());
		}
		String doLogin = "", line;
		while((line = br.readLine()) != null)
			doLogin += line;
		br.close();
		bw.close();

		boolean success = extractGSONBooleanValue(doLogin, "success");
		String message = extractGSONStringValue(doLogin, "message");
		if(!success) {
			if(message.equals("Please verify your humanity by re-entering the characters in the captcha below."))
				throw new CaptchaException(extractGSONLongValue(doLogin, "captcha_gid"));
			throw new Throwable(message);
		}
		
		List<String> cookies = con.getHeaderFields().get("Set-Cookie");
		String cookiesStr = "";
		for(String s : cookies)
			cookiesStr += s + ";";
		if(cookiesStr.endsWith(";"))
			cookiesStr = cookiesStr.substring(0, cookiesStr.length() - 1);
		
		return new Object[] {
				cookiesStr,
				doLogin
		};
	}
	
	/**
	 * @param username
	 * @return 0 = rsa pubkey, 1 = rsa timestamp
	 * @throws Throwable
	 */
	public static Object[] getRSAKey(String username) throws Throwable {
		POSTData postData = new POSTData();
		postData.put("username", username);
		String postStr = postData.toString();
		String json = post("https://steamcommunity.com/login/getrsakey", postStr);
		
		return new Object[] {
			args2RSA (
				new BigInteger (
					extractGSONStringValue (
						json,
						"publickey_mod"
					),
					16
				),
				new BigInteger (
					extractGSONStringValue (
						json,
						"publickey_exp"
					),
					16
				)
			),
			extractGSONLongValue (
				json,
				"timestamp"
			)
		};
	}
	
	public static String post(String url, String postStr) throws Throwable {
		HttpsURLConnection con = (HttpsURLConnection) new URL(url).openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestMethod("POST");
		con.setRequestProperty("Content-Length", Integer.toString(postStr.length()));
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
		con.connect();
		bw.write(postStr);
		bw.flush();
		
		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} catch(Throwable t) {
			br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			System.err.println(t.getMessage());
		}
		String data = "", line;
		while((line = br.readLine()) != null)
			data += line;
		br.close();
		bw.close();
		
		return data;
	}
	
	public static PublicKey args2RSA(BigInteger modulus, BigInteger exponent) throws Throwable {
		RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		return factory.generatePublic(spec);
	}
}
