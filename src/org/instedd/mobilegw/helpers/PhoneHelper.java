package org.instedd.mobilegw.helpers;

public class PhoneHelper {

	public static String removeNonNumeric(String number) {
		return number.replaceAll("[^\\d]", "");
	}
	
	public static String removeLeadingPlus(String number) {
		return number.replaceAll("^\\+", "");
	}
	
	/**
	 * Removes all non numeric characters from phone number and prepends protocol sms://
	 */
	public static String withSmsProtocol(String number) {
		return "sms://"+ removeNonNumeric(number);
	}
}
