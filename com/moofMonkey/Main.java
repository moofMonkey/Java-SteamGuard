package com.moofMonkey;

import java.io.File;

import com.moofMonkey.steam.Properties;
import com.moofMonkey.steam.SteamBase;
import com.moofMonkey.steam.SteamCodeGenerator;
import com.moofMonkey.steam.SteamCookies;
import com.moofMonkey.steam.SteamMobileConfirmations;

public class Main {
	public static void main(String[] args) throws Throwable {
		if(args.length < 2) {
			System.out.println("Usage: java -jar SteamGuard.jar <config> <conf/url/code> [url]");
			System.exit(0);
		}
		
		File settings = new File(args[0]);
		Properties props = Properties.getProps(settings);
		
		switch(args[1]) {
			case "conf":
				SteamMobileConfirmations conf = new SteamMobileConfirmations(props, SteamCookies.getData(props));
				for(String s : conf.getNewResponse())
					System.out.println(s);
				break;
			case "url":
				new SteamBase().getResponse(args[2], (String) SteamCookies.getData(props)[0]);
				break;
			case "code":
				System.out.println(new SteamCodeGenerator(props).generateSteamGuardCode());
				break;
			default:
				System.out.println("There are only methods [conf, url, code]");
		}
	}
}
