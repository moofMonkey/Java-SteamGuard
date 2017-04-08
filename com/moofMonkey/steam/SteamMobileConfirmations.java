package com.moofMonkey.steam;

import java.math.BigInteger;
import java.util.ArrayList;

public class SteamMobileConfirmations extends SteamBase {
	public enum EConfirm {
		ALLOW("allow"),
		DISALLOW("cancel");

		public String value;

		private EConfirm(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}
	
	public class Confirmation {
		public BigInteger id, key;
		public String desc = "", action = "", avatar = "";

		public Confirmation(BigInteger _id, BigInteger _key) {
			id = _id;
			key = _key;
		}

		public Confirmation(long _id, BigInteger _key) {
			id = BigInteger.valueOf(_id);
			key = _key;
		}
		
		public Confirmation setAction(String action) {
			this.action = action;
			return this;
		}
		
		public Confirmation setDesc(String desc) {
			this.desc = desc;
			return this;
		}
		
		public Confirmation setAvatar(String miniAvatar) {
			this.avatar = miniAvatar;
			return this;
		}
		
		public String getURL(EConfirm value) throws Throwable {
			return URL_COMMUNITY_BASE + "/mobileconf/ajaxop?" + "op=" + value + "&" + getConfirmationParams(value) + "&cid="
					+ id + "&ck=" + key;
		}
	}

	private String browser_cookies;
	private long STEAMID64;
	public SteamMobileConfirmations(Properties _props) throws Throwable {
		props = _props;
		browser_cookies = props.browser_cookies;
		STEAMID64 = props.steamid64;
	}

	public static final String METHOD = "conf";
	public static final String URL_COMMUNITY_BASE = "https://steamcommunity.com";
	public static final String CONFIRMATION_WEB = URL_COMMUNITY_BASE + "/" + "mobileconf" + "/" + METHOD;
	private Properties props;
	
	public static String getFullAvatar(String miniAvatar) {
		if(miniAvatar.matches(".*_full\\.jpg"))
			return miniAvatar;
		
		return miniAvatar.replaceAll("\\.jpg", "_full.jpg");
	}
	
	public static String getMiniAvatar(String fullAvatar) {
		return fullAvatar.replaceAll("_full\\.jpg", ".jpg");
	}

	public String base64encryptedConfirmationHash(long time, String tag) throws Throwable {
		byte[] secretBytes = _Base64.FromBase64String(props.identify_secret);
		int dataLen = 8;

		if (tag != null)
			if (tag.length() > 32)
				dataLen += 32;
			else
				dataLen += tag.length();

		byte[] dataBytes = new byte[dataLen];
		int var = 8;

		for (int i = var - 1; i != 0; --i) {
			dataBytes[i] = (byte) time;
			time >>>= var;
		}

		if (tag != null)
			System.arraycopy(tag.getBytes(), 0, dataBytes, 8, dataLen - 8);

		return encodeUrlUnsafeChars (
				new String(_Base64.ToBase64String(_HMACSHA1.encode(secretBytes, dataBytes)))
		);
	}

	public String getConfirmationParams(Object tag) throws Throwable {
		String strTag = tag.toString();
		if (base64encryptedConfirmationHash(tc.currentTimeSeconds(), strTag) == null)
			return "";
		return String.format("p=%s&a=%s&k=%s&t=%d&m=android&tag=%s",
				new Object[] { "android:" + props.machineName, STEAMID64,
						base64encryptedConfirmationHash(tc.currentTimeSeconds(), strTag),
						Long.valueOf(tc.currentTimeSeconds()), tag });
	}

	public ArrayList<Confirmation> getConfirmations(ArrayList<String> response) {
		ArrayList<Confirmation> confs = new ArrayList<Confirmation>();
		String s1;
		boolean data = false;
		int dataI = -1;
		int avatarI = -1;
		boolean findedMyAvatar = false;

		for (int i = 0; i < response.size(); i++) {
			String s = response.get(i);
			
			
			if(s.indexOf("<div class=\"mobileconf_list_entry_content\">") > -1)
				data = true;
			
			if(data) {
				if (s.indexOf("<div class=\"mobileconf_list_entry_description\"") > -1) {
					dataI++;
					String desc = "";
					String line;
					confs.set
						(
							dataI,
							confs.get(dataI)
							.setAction
								(
									getInfo(response.get(++i))
								)
						);
					while(!(line = getInfo(response.get(++i))).equals("</div>"))
						desc += line + "\n";
					i--;
					confs.set
						(
							dataI,
							confs.get(dataI)
								.setDesc
									(
										desc
									)
						);
				}
				
				if (s.indexOf("<div class=\"playerAvatar") > -1) {
					avatarI++;
					confs.set
						(
							avatarI,
							confs.get(avatarI)
								.setAvatar
									(
										extractStringValue(s, "src")
									)
						);
				}
			} else {
				if(!findedMyAvatar) {
					if (s.indexOf("<img src=\"") > -1) {
						s1 = s.trim();
						//myAvatar = extractStringValue(s1, "src");
						findedMyAvatar = true;
					}
				} else
					if (s.indexOf("<div class=\"mobileconf_list_entry\"") > -1) {
						s1 = s.trim();
						confs.add(new Confirmation(extractBigIntegerValue(s1, "data-confid"),
								extractBigIntegerValue(s1, "data-key")));
					}
			}
		}

		return confs;
	}
	
	public static String getInfo(String str) {
		if(str.trim().equals("</div>"))
			return "</div>";
		
		return
				str
					.replaceAll("\\<div\\>", "")
					.replaceAll("\\<\\/div\\>", "")
					.trim();
	}

	public ArrayList<String> getConfirmationInfos(ArrayList<Confirmation> confs) throws Throwable {
		ArrayList<String> strs = new ArrayList<String>();

		if (confs.size() == 0)
			strs.add("No confirmations found");
		else
			for (Confirmation s : confs) {
				strs.add("-----------------");
				strs.add(s.action + "\n" + s.desc + s.avatar);
				strs.add("Accept: " + s.getURL(EConfirm.ALLOW));
				strs.add("Cancel: " + s.getURL(EConfirm.DISALLOW));
				strs.add("-----------------");
			}

		return strs;
	}
	
	public ArrayList<String> getNewResponse() throws Throwable {
		String toFind = CONFIRMATION_WEB + "?" + getConfirmationParams(METHOD);
		
		try {
			return getConfirmationInfos(getConfirmations(getResponse(toFind, browser_cookies)));
		} catch(java.net.MalformedURLException t) {
			if(t.getMessage().indexOf("steammobile") > -1) {
				browser_cookies = (String) SteamCookies.getData(props)[0];
				props.browser_cookies = browser_cookies;
				props.saveProps();
				return getNewResponse();
			}
		}
		return null;
	}
}
