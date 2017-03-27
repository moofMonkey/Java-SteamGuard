package com.moofMonkey.steam;

public class CaptchaException extends Exception {
	private static final long serialVersionUID = 1L;
	public String captchaURL;
	public long captchaGID;
	public CaptchaException(long _captchaGID) {
		captchaGID = _captchaGID;
		captchaURL = "https://steamcommunity.com/login/rendercaptcha/?gid=" + _captchaGID;
	}
}
