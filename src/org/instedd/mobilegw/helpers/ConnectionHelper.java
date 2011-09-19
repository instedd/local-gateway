package org.instedd.mobilegw.helpers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class ConnectionHelper {

	public static HttpURLConnection openConnection(URL baseAddress, String path) throws IOException {
		return openConnection(baseAddress, path, null, null);
	}
	
	public static HttpURLConnection openConnection(URL baseAddress, String path, String username, String password) throws IOException {
		URL url;

		try {
			url = new URL(baseAddress, path);
		} catch (MalformedURLException e) {
			throw new Error(e);
		}

		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setConnectTimeout(10000);
		
		// Basic authentication
		if (username != null) {
			String userAndPass = username + ":" + password;
	        String encoding = Base64.encodeBytes(userAndPass.getBytes(), Base64.DONT_BREAK_LINES);
	        connection.setRequestProperty("Authorization", "Basic " + encoding);
		}
        
		return connection;
	}

	
}
