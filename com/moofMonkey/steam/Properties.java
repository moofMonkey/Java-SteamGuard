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
	public String identify_secret, shared_secret, machineName, username, password;
	
	public Properties (
			String _identify_secret,
			String _shared_secret,
			String _machineName,
			String _username,
			String _password
	) throws Throwable {
		identify_secret = _identify_secret;
		shared_secret = _shared_secret;
		machineName = _machineName;
		username = _username;
		password = _password;
	}
	
	public static Properties getProps(File settings) throws Throwable {
		String identify_secret = "", shared_secret = "", machineName = "", userName = "", password = "";
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
			
			props = new Properties(identify_secret, shared_secret, machineName, userName, password);
			props.saveProps(settings);
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
		}

		return
				props == null
					? new Properties(identify_secret, shared_secret, machineName, userName, password)
					: props;
	}

	public void saveProps(File settings) throws Throwable {
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
				+ "}";
	}
}
