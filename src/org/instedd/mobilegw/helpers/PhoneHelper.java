package org.instedd.mobilegw.helpers;

public class PhoneHelper {

	/**
	 * Removes all non numeric characters from the number, including the protocol
	 */
	public static String removeNonNumeric(String number) {
		return number.replaceAll("[^\\d]", "");
	}
	
	/**
	 * Removes the leading plus from the number, if is set
	 */
	public static String removeLeadingPlus(String number) {
		return number.replaceAll("^\\+", "");
	}
	
	/**
	 * Removes all non numeric characters from phone number and prepends protocol sms://
	 */
	public static String withSmsProtocol(String number) {
		return "sms://"+ removeNonNumeric(number);
	}
	
	/**
	 * Removes all non numeric characters and protocol and adds a leading plus symbol
	 */
	public static String withLeadingPlus(String number) {
		return "+" + removeNonNumeric(number);
	}
}
