package com.moofMonkey.steam;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TimeCorrector {
	private static TimeCorrector s_instance;
	private long m_SkewToleranceSeconds;
	private long m_timeAdjustment;

	public static URL TWO_FACTOR_TIME_QUERY;
	static {
		try {
			TWO_FACTOR_TIME_QUERY = new URL("https://api.steampowered.com/ITwoFactorService/QueryTime/v1");
		} catch(Throwable t) { t.printStackTrace(); }
	}

	public static TimeCorrector getInstance() {
		if (s_instance == null)
			s_instance = new TimeCorrector();

		return s_instance;
	}

	private int delta = 0;

	public TimeCorrector() {
		alignTime();
	}

	public void alignTime() {
		try {
			long requestStartTime = System.currentTimeMillis();
			String response = getJSON();
			long serverTime = extractLongValue(response, "server_time", 0, 1, 5133808000L);
			boolean bUseServerTime = true;
			long now = System.currentTimeMillis();
			extractSyncParameters(response);
			delta = (int) (serverTime - now);

			if (now < requestStartTime)
				bUseServerTime = false;
			if (serverTime < 1418057957 || serverTime > 4133808000L)
				bUseServerTime = false;
			if (now - requestStartTime > 10)
				bUseServerTime = false;
			if (delta < m_SkewToleranceSeconds)
				bUseServerTime = false;
			if (!bUseServerTime)
				delta = 0;
			m_timeAdjustment = delta;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getJSON() {
		try {
			HttpURLConnection con = (HttpURLConnection) TWO_FACTOR_TIME_QUERY.openConnection();
			String urlParameters = "steamid=0";
			String response = "";
			String line;

			// Send post request
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			DataOutputStream dos = new DataOutputStream(con.getOutputStream());
			dos.writeBytes(urlParameters);
			dos.flush();
			dos.close();
			DataInputStream dis = new DataInputStream(con.getInputStream());
			while ((line = dis.readLine()) != null)
				response += line;
			dis.close();

			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public long currentTimeSeconds() {
		return System.currentTimeMillis() / 1000 + m_timeAdjustment;
	}

	public static long extractLongValue(String JSON, String value, long defaultValue, long minValue, long maxValue) {
		char[] json = JSON.toCharArray();
		String tmp = "";
		boolean finded = false;

		for (int i = JSON.indexOf(value); i < json.length; i++)
			try {
				char element = json[i];
				Integer.parseInt("" + element);
				tmp += element;
				finded = true;
			} catch (Throwable t) {
				if (finded)
					break;
				else
					continue;
			}

		return Long.parseLong(tmp);
	}

	private void extractSyncParameters(String json) {
		m_SkewToleranceSeconds = extractLongValue(json, "skew_tolerance_seconds", m_SkewToleranceSeconds, 10, 300);
	}
}
