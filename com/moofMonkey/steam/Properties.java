package com.moofMonkey.steam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;

public class Properties extends SteamBase {
	public String identify_secret, shared_secret, machineName, username, password, browser_cookies;
	public long steamid64;
	private File settings;
	
	public Properties ( // For creating
			String _identify_secret,
			String _shared_secret,
			String _machineName,
			String _username,
			String _password,
			File _settings
	) throws Throwable {
		this (
			_identify_secret,
			_shared_secret,
			_machineName,
			_username,
			_password,
			0,
			"",
			_settings
		);
	}
	
	public Properties ( // For importing
			String _identify_secret,
			String _shared_secret,
			String _machineName,
			String _username,
			String _password,
			long _steamid64,
			String _browser_cookies,
			File _settings
	) throws Throwable {
		identify_secret = _identify_secret;
		shared_secret = _shared_secret;
		machineName = _machineName;
		username = _username;
		password = _password;
		steamid64 = _steamid64;
		browser_cookies = _browser_cookies;
		settings = _settings;
	}
	
	public static Properties getProps(File settings) throws Throwable {
		String identify_secret = "", shared_secret = "", machineName = "", userName = "", password = "", browser_cookies = "";
		long steamid64 = 0;
		BufferedReader in;

		Properties props = null;
		if (!settings.exists()) {
			in = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Please write your username");
			userName = in.readLine();
			System.out.println("Please write your password");
			password = in.readLine();
			System.out.println("Please write your identify_secret");
			identify_secret = in.readLine();
			System.out.println("Please write your shared_secret");
			shared_secret = in.readLine();
			System.out.println("Please write your machineName (optional, you can just press ENTER)");
			machineName = in.readLine();
			
			if(machineName.length() == 0) {
				SecureRandom secRand = new SecureRandom();
				byte[] b = new byte[256];
				secRand.nextBytes(b);
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				machineName = DatatypeConverter.printHexBinary(md.digest(b));
			}
			in.close();
			
			props = new Properties(identify_secret, shared_secret, machineName, userName, password, settings);
			Object[] data = SteamCookies.getData(props);
			props.browser_cookies = (String) data[0];
			props.steamid64 = extractGSONLongValue((String) data[1], "steamid");
			props.settings = settings;
			props.saveProps();
		} else {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(settings)));
			String base64 = "";
			String line;
			while ((line = in.readLine()) != null)
				base64 += line;
			in.close();
			String json = new String(_Base64.FromBase64String(base64));
			identify_secret = extractStringValue(json, "identify_secret");
			shared_secret = extractStringValue(json, "shared_secret");
			machineName = extractStringValue(json, "machineName");
			userName = extractStringValue(json, "username");
			password = extractStringValue(json, "password");
			steamid64 = extractLongValue(json, "steamid64");
			browser_cookies = extractStringValue(json, "browser_cookies");
		}

		return
				props == null
					? new Properties(identify_secret, shared_secret, machineName, userName, password, steamid64, browser_cookies, settings)
					: props;
	}

	public void saveProps() throws Throwable {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(settings)));
		bw.write(_Base64.ToBase64String(toString().getBytes()));
		bw.close();
	}

	@Override
	public String toString() {
		return    "{"
				+ "identify_secret=\"" + identify_secret + "\", "
				+ "shared_secret=\"" + shared_secret + "\", "
				+ "machineName=\"" + machineName + "\", "
				+ "username=\"" + username + "\", "
				+ "password=\"" + password + "\", "
				+ "steamid64=\"" + steamid64 + "\", "
				+ "browser_cookies=\"" + browser_cookies + "\", "
				+ "}";
	}
}
