package org.instedd.mobilegw.helpers;

public class PhoneHelper {

	/**
	 * Removes all non numeric characters from the number, including the protocol
	 */
	public static String removeNonNumeric(String number) {
		if (number == null) return null;
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
	 * Removes all non numeric characters from phone number and prepends protocol sms://
	 * Uses defaultValue if number is null or empty
	 */
	public static String withSmsProtocol(String number, String defaultValue) {
		number = removeNonNumeric(number);
		if (number == null || number.length() == 0) number = defaultValue;
		return "sms://" + number;
	}
	
	/**
	 * Removes all non numeric characters and protocol and adds a leading plus symbol
	 */
	public static String withLeadingPlus(String number) {
		return "+" + removeNonNumeric(number);
	}

	/**
	 * Returns whether the phone number is valid (contains only numeric chars)
	 */
	public static boolean isValidNumericPhone(String phoneNumber) {
		return phoneNumber.matches("^\\d+$");
	}
}
