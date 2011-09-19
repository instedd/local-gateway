package org.instedd.mobilegw.messaging;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

public class MessageMarshaller
{

	public static Message[] fromXml(InputStream in)
	{
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Messages.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Messages messages = (Messages) unmarshaller.unmarshal(in);
			return messages.messages;
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void toXml(Message[] messages, OutputStream out)
	{
		Messages object = new Messages();
		object.messages = messages;

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Messages.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(object, out);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@XmlRootElement()
	static class Messages
	{
		@XmlElement(name = "message")
		public Message[] messages;
	};
}
