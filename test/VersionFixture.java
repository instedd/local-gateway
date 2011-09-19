

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.instedd.mobilegw.updater.Version;
import org.junit.Test;

public class VersionFixture
{
	@Test
	public void testParseVersion() throws Exception
	{
		Version version = Version.parse("1.2.3.4");
		assertArrayEquals(new int[] { 1, 2, 3, 4}, version.getDigits());
	}
	
	@Test
	public void testCompareEqualVersions() throws Exception
	{
		Version v1 = Version.parse("1.2.3.4");
		Version v2 = Version.parse("1.2.3.4");
		assertEquals(0, v1.compareTo(v2));
	}
	
	@Test
	public void testCompareToGreaterVersion() throws Exception
	{
		Version v1 = Version.parse("1.2.3.4");
		Version v2 = Version.parse("1.2.4.4");
		assertEquals(1, v1.compareTo(v2));
	}
	
	@Test
	public void testCompareToLessVersion() throws Exception
	{
		Version v1 = Version.parse("1.2.3.4");
		Version v2 = Version.parse("1.1.2.3");
		assertEquals(-1, v1.compareTo(v2));
	}
	
	@Test
	public void testCompareToShorterVersion() throws Exception
	{
		Version v1 = Version.parse("1.2.3.4");
		Version v2 = Version.parse("1.2.3");
		assertEquals(-1, v1.compareTo(v2));
	}
	
	@Test
	public void testCompareToLargerVersion() throws Exception
	{
		Version v1 = Version.parse("1.2.3");
		Version v2 = Version.parse("1.2.3.4");
		assertEquals(1, v1.compareTo(v2));
	}
}
