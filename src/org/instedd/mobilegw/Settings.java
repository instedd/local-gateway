package org.instedd.mobilegw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class Settings implements Cloneable {
	private static final int MODEM_BAUDRATE_DEFAULT = 9600;
	private static final String GATEWAY_URL = "gateway.url";
	private static final String GATEWAY_USERNAME = "gateway.username";
	private static final String GATEWAY_PASSWORD = "gateway.password";
	private static final String HTTP_PROXY_HOST = "http.proxyHost";
	private static final String HTTP_PROXY_PORT = "http.proxyPort";
	private static final String HTTPS_PROXY_HOST = "https.proxyHost";
	private static final String HTTPS_PROXY_PORT = "https.proxyPort";
	private static final String MODEM_COMPORT = "modem.comport";
	private static final String MODEM_BAUDRATE = "modem.baudrate";
	private static final String MODEM_MANUFACTURER = "modem.manufacturer";
	private static final String MODEM_MODEL = "modem.model";
	private static final String MODEM_APPEND_PLUS = "model.appendPlus";
	private static final String MODEM_MOCK_MESSAGES_MODE = "modem.mockmessages";
	private static final String MOBILE_NUMBER = "mobile.number";
	private static final String SKYPE_ENABLED = "skype.enabled";
	
	private Properties properties;

	public Settings() {
		properties = new Properties();

		try {
			FileInputStream file = new FileInputStream(getSettingsFile());
			try {
				properties.load(file);
			} finally {
				file.close();
			}
		} catch (IOException e) {
			// Ignore the error and continue with empty settings
		}
	}
	
	private Settings(Properties properties) {
		this.properties = properties;
	}
	
	public Settings clone() {
		return new Settings(this.properties);
	}

	public void save() throws IOException {
		FileOutputStream file = new FileOutputStream(getSettingsFile());
		try {
			properties.store(file, null);
		} finally {
			file.close();
		}
	}

	private File getSettingsFile() {
		return new File(Main.getWorkingDirectory(), "sms.properties");
	}

	public void applySystemProperties() {
		System.setProperty("http.proxyHost", this.getHttpProxyHost());
		System.setProperty("http.proxyPort", this.getHttpProxyPort());
		System.setProperty("https.proxyHost", this.getHttpsProxyHost());
		System.setProperty("https.proxyPort", this.getHttpsProxyPort());
	}

	public void setComPort(String port) {
		properties.setProperty(MODEM_COMPORT, port);
	}

	public String getComPort() {
		return properties.getProperty(MODEM_COMPORT);
	}

	public void setComBaudRate(int baudRate) {
		properties.setProperty(MODEM_BAUDRATE, Integer.toString(baudRate));
	}

	public int getComBaudRate() {
		String baudRate = properties.getProperty(MODEM_BAUDRATE);
		try {
			return Integer.parseInt(baudRate);
		} catch (NumberFormatException e) {
			return MODEM_BAUDRATE_DEFAULT;
		}
	}

	public void setModemManufacturer(String manufacturer) {
		properties.setProperty(MODEM_MANUFACTURER, manufacturer);
	}

	public String getModemManufacturer() {
		return properties.getProperty(MODEM_MANUFACTURER);
	}

	public void setModemModel(String model) {
		properties.setProperty(MODEM_MODEL, model);
	}

	public String getModemModel() {
		return properties.getProperty(MODEM_MODEL);
	}

	public void setMobileNumber(String mobileNumber) {
		properties.setProperty(MOBILE_NUMBER, mobileNumber);
	}

	public String getMobileNumber() {
		return properties.getProperty(MOBILE_NUMBER);
	}

	public void setGatewayUrl(String url) {
		properties.setProperty(GATEWAY_URL, url);
	}

	public String getGatewayUrl() {
		return properties.getProperty(GATEWAY_URL);
	}

	public void setGatewayUsername(String username) {
		properties.setProperty(GATEWAY_USERNAME, username);
	}

	public String getGatewayUsername() {
		return properties.getProperty(GATEWAY_USERNAME);
	}

	public void setGatewayPassword(String password) {
		properties.setProperty(GATEWAY_PASSWORD, password);
	}

	public String getGatewayPassword() {
		return properties.getProperty(GATEWAY_PASSWORD);
	}

	public void setSkypeEnabled(boolean enabled) {
		properties.setProperty(SKYPE_ENABLED, Boolean.toString(enabled));
	}

	public boolean getSkypeEnabled() {
		return Boolean.parseBoolean(properties.getProperty(SKYPE_ENABLED));
	}

	public String getHttpProxyHost() {
		return properties.getProperty(HTTP_PROXY_HOST);
	}

	public void setHttpProxyHost(String host) {
		properties.setProperty(HTTP_PROXY_HOST, host);
	}

	public String getHttpProxyPort() {
		return properties.getProperty(HTTP_PROXY_PORT);
	}

	public void setHttpProxyPort(String port) {
		properties.setProperty(HTTP_PROXY_PORT, port);
	}

	public String getHttpsProxyHost() {
		return properties.getProperty(HTTPS_PROXY_HOST);
	}

	public void setHttpsProxyHost(String host) {
		properties.setProperty(HTTPS_PROXY_HOST, host);
	}

	public String getHttpsProxyPort() {
		return properties.getProperty(HTTPS_PROXY_PORT);
	}

	public void setHttpsProxyPort(String port) {
		properties.setProperty(HTTPS_PROXY_PORT, port);
	}
	
	public boolean getAppendPlus() {
		return Boolean.parseBoolean(properties.getProperty(MODEM_APPEND_PLUS));
	}
	
	public void setAppendPlus(boolean appendPlus) {
		properties.setProperty(MODEM_APPEND_PLUS, Boolean.toString(appendPlus));
	}
	
	public URL getNuntiumUrl() throws MalformedURLException {
		return new URL(System.getProperty("LGW_NUNTIUM_URL", "http://nuntium.instedd.org"));
	}

	public boolean getMockMessagesMode() {
		return Boolean.parseBoolean(properties.getProperty(MODEM_MOCK_MESSAGES_MODE));
	}
	
	public void setMockMessagesMode(boolean value) {
		properties.setProperty(MODEM_MOCK_MESSAGES_MODE, Boolean.toString(value));
	}
}
