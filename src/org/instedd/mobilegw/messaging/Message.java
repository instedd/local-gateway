package org.instedd.mobilegw.messaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

public class Message
{
	@XmlAttribute
	public String id;
	@XmlTransient
	public int retries;
	@XmlTransient
	public Date retryTime;
	@XmlAttribute
	public String to;
	@XmlAttribute
	public String from;
	@XmlAttribute
	@XmlJavaTypeAdapter(value = MessageDateAdapter.class)
	public Date when;
	@XmlElement
	public String text;

	public static class MessageDateAdapter extends XmlAdapter<String, Date>
	{
		SimpleDateFormat dateFormat;
		public MessageDateAdapter()
		{
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		@Override
		public String marshal(Date value) throws Exception
		{
			return dateFormat.format(value);
		}

		@Override
		public Date unmarshal(String value) throws Exception
		{
			return dateFormat.parse(value);
		}
	}
	
	@Override
	public String toString()
	{
		return "Id: " + id + ", To: " + to;
	}
}
