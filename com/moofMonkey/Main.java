package com.moofMonkey;

import java.io.File;

import com.moofMonkey.steam.Properties;
import com.moofMonkey.steam.SteamBase;
import com.moofMonkey.steam.SteamCodeGenerator;
import com.moofMonkey.steam.SteamMobileConfirmations;
import com.moofMonkey.steam.SteamMobileConfirmations.Confirmation;

public class Main {
	public static void main(String[] args) throws Throwable {
		if(args.length < 2) {
			System.out.println("Usage: java -jar SteamGuard.jar <config> <conf/url/code/cookies> [url]");
			System.exit(0);
		}
		
		File settings = new File(args[0]);
		Properties props = Properties.getProps(settings);
		
		switch(args[1]) {
			case "conf":
				if(args.length == 3)
					if(args[2].equalsIgnoreCase("acceptall")) {
						SteamMobileConfirmations conf = new SteamMobileConfirmations(props);
						for(Confirmation iConf : conf.getConfirmations())
							iConf.accept(props);
						break;
					}
				SteamMobileConfirmations conf = new SteamMobileConfirmations(props);
				for(String s : conf.getNewResponse())
					System.out.println(s);
				break;
			case "url":
				System.out.println(new SteamBase().getResponse(args[2], props.browser_cookies));
				break;
			case "code":
				System.out.println(new SteamCodeGenerator(props).generateSteamGuardCode());
				break;
			case "cookies":
				System.out.println(props.browser_cookies);
				break;
			default:
				System.out.println("There are only methods [conf, url, code, cookies]");
		}
	}
}
