package org.instedd.mobilegw.updater;

public class Version
{
	private int[] digits;
	
	public Version(int[] versionDigits)
	{
		this.digits = versionDigits;
	}
	
	public static Version parse(String version) {
		String[] split = version.split("\\.");
		int[] parsedVersion = new int[split.length];
		
		for (int i = 0; i < split.length; i++) {
			parsedVersion[i] = Integer.parseInt(split[i]);
		}
		return new Version(parsedVersion);
	}
	
	public int[] getDigits()
	{
		return digits;
	}

	public int compareTo(Version other)
	{
		for (int i = 0; i < digits.length && i < other.digits.length; i++) {
			if (digits[i] > other.digits[i])
				return -1;
			if (digits[i] < other.digits[i])
				return 1;
		}
		
		if (digits.length > other.digits.length)
			return -1;
		if (digits.length < other.digits.length)
			return 1;
		return 0;
	}
	
	@Override
	public String toString()
	{
		if (digits.length == 0)
			return "";
		StringBuilder buffer = new StringBuilder(Integer.toString(digits[0]));
		for (int i = 1; i < digits.length; i++) {
			buffer.append(".").append(Integer.toString(digits[i]));
		}
		return buffer.toString();
	}
}
