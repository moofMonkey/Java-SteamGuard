package com.moofMonkey.steam;

public class SteamCodeGenerator extends SteamBase {
	private static byte[] steamGuardCodeTranslations = "23456789BCDFGHJKMNPQRTVWXY".getBytes();
	private Properties props;
	
	public SteamCodeGenerator(Properties _props) {
		props = _props;
	}

	public static final long currentTime() {
		return tc.currentTimeSeconds();
	}

	public String generateSteamGuardCode() throws Throwable {
		return generateSteamGuardCodeForTime(tc.currentTimeSeconds());
	}

	public String generateSteamGuardCodeForTime(long time) throws Throwable {
		byte[] sharedSecretArray = _Base64.FromBase64String(props.shared_secret);

		time /= 30;

		byte[] timeArray = new byte[8];

		for (int i = 8; i > 0; i--) {
			timeArray[i - 1] = (byte) (int) time;
			time >>>= 8;
		}

		byte[] hmac_result = _HMACSHA1.encode(sharedSecretArray, timeArray);
		byte[] codeArray = new byte[5];
		try {
			int offset = hmac_result[19] & 15;
			int bin_code = (hmac_result[offset] & 127) << 24 | (hmac_result[offset + 1] & 255) << 16
					| (hmac_result[offset + 2] & 255) << 8 | hmac_result[offset + 3] & 255;

			for (int i = 0; i < 5; i++) {
				codeArray[i] = steamGuardCodeTranslations[bin_code % steamGuardCodeTranslations.length];
				bin_code /= steamGuardCodeTranslations.length;
			}

			return new String(codeArray);
		} catch (Exception ex) {
			return null;
		}
	}

	public static final int secondsToNextChange() {
		return (int) (30 - currentTime() % 30);
	}
}
