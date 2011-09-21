package org.instedd.mobilegw.helpers;

public class PhoneHelper {

	public static String removeLeadingPlus(String number) {
		if (number.startsWith("+")) {
			return number.substring(1);
		} else {
			return number;
		}
	}
	
	public static String withSmsProtocol(String number) {
		number = removeLeadingPlus(number);
		if (number.startsWith("sms://")) {
			return number;
		} else {
			return "sms://" + number;
		}
	}
}
