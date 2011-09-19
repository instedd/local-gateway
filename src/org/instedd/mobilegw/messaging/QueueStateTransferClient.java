package org.instedd.mobilegw.messaging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.instedd.mobilegw.helpers.ConnectionHelper;

public class QueueStateTransferClient
{

	private final URL baseAddress;
	private final String username;
	private final String password;

	public QueueStateTransferClient(URL baseAddress, String username, String password)
	{
		this.username = username;
		this.password = password;
		
		if (!baseAddress.getPath().endsWith("/"))
		{
			try {
				this.baseAddress = new URL(baseAddress.toString() + "/");
			} catch (MalformedURLException e) {
				throw new Error(e);
			}
		}
		else
		{
			this.baseAddress = baseAddress;
		}
	}

	private HttpURLConnection openConnection(String path) throws IOException {
		return ConnectionHelper.openConnection(baseAddress, path, username, password);
	}

	public String send(Message[] messages) throws IOException
	{
		HttpURLConnection connection = openConnection("incoming");
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "text/xml");
		connection.connect();

		try {
			OutputStream outputStream = connection.getOutputStream();
			MessageMarshaller.toXml(messages, outputStream);
			outputStream.close();

			if (connection.getResponseCode() != 200)
				throw new IOException(connection.getResponseMessage());

			return connection.getHeaderField("ETag");
		} finally {
			connection.disconnect();
		}
	}

	public String getLastSentMessageId() throws IOException
	{
		HttpURLConnection connection = openConnection("incoming");
		connection.setRequestMethod("HEAD");
		connection.connect();

		try {
			if (connection.getResponseCode() != 200)
				throw new IOException(connection.getResponseMessage());

			return connection.getHeaderField("ETag");
		} finally {
			connection.disconnect();
		}
	}

	public Message[] receive(int max, String lastReceivedMessageId) throws IOException
	{
		HttpURLConnection connection = openConnection("outgoing?max=" + max);
		if (lastReceivedMessageId != null)
			connection.setRequestProperty("If-None-Match", lastReceivedMessageId);
		connection.connect();

		try {
			if (connection.getResponseCode() != 200)
				throw new IOException(connection.getResponseMessage());

			Message[] messages = MessageMarshaller.fromXml(connection.getInputStream());
			return messages;
		} finally {
			connection.disconnect();
		}
	}
	
	public void setAddress(String newAddress) throws IOException
	{
		HttpURLConnection connection = openConnection("setaddress?address=" + newAddress);
		connection.connect();
		
		try {
			if (connection.getResponseCode() == 404)
				throw new UnsupportedOperationException();
			if (connection.getResponseCode() != 200)
				throw new IOException(connection.getResponseMessage());
		} finally {
			connection.disconnect();
		}
	}

}
