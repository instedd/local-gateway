package org.instedd.mobilegw.messaging;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class DbMessageQueue extends AbstractMessageQueue
{
	private Connection connection;
	private final String table;
	private PreparedStatement enqueueStmt;
	private PreparedStatement deleteBatchStmt;
	private PreparedStatement dequeueStmt;
	private PreparedStatement setRetryStmt;
	private PreparedStatement deleteStmt;
	private PreparedStatement selectAllStmt;
	private PreparedStatement countStmt;
	private PreparedStatement forceStmt;

	public DbMessageQueue(Connection connection, String table)
	{
		this.table = table;
		try {
			this.connection = connection;
			ensureTable();

			enqueueStmt = connection.prepareStatement("INSERT INTO " + table + " (Id, [When], [From], [To], [Text]) VALUES (?, ?, ?, ? ,?)");
			deleteBatchStmt = connection.prepareStatement("DELETE FROM " + table + " WHERE [OrderId] <= (SELECT [OrderId] FROM " + table + " WHERE Id = ?)");
			dequeueStmt = connection.prepareStatement("SELECT * FROM " + table + " WHERE [RetryTime] IS NULL OR [RetryTime] <= ? LIMIT ?");
			setRetryStmt = connection.prepareStatement("UPDATE " + table + " SET [RetryTime] = ?, [Retries] = [Retries] + 1 WHERE Id = ?");
			deleteStmt = connection.prepareStatement("DELETE FROM " + table + " WHERE Id = ?");
			selectAllStmt = connection.prepareStatement("SELECT * FROM " + table + " ORDER BY [When]");
			countStmt = connection.prepareStatement("SELECT COUNT(*) FROM " + table);
			forceStmt = connection.prepareStatement("UPDATE " + table + " SET [RetryTime] = NULL WHERE Id = ?");

		} catch (SQLException e) {
			throw new Error(e);
		}
	}

	private void ensureTable() throws SQLException
	{
		ResultSet tables = connection.getMetaData().getTables(null, null, table, null);
		try
		{
			if (!tables.next())
			{
				Statement stmt = connection.createStatement();
				stmt.execute("CREATE TABLE " + table + " (\n" + 
						"[OrderId] INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
						"[Id] TEXT UNIQUE NOT NULL,\n" +
						"[When] INTEGER NOT NULL,\n" +
						"[From] TEXT NOT NULL,\n" +
						"[To] TEXT NOT NULL,\n" +
						"[Text] TEXT NOT NULL,\n" +
						"[Retries] INTEGER NOT NULL DEFAULT 0,\n" +
				"[RetryTime] INTEGER)");
				connection.commit();
			}
		}
		finally
		{
			tables.close();
		}
	}

	@Override
	public void deleteBatch(String untilIdInclusive) throws Exception
	{
		synchronized (connection)
		{
			deleteBatchStmt.setString(1, untilIdInclusive);
			deleteBatchStmt.execute();
			connection.commit();
		}
		onMessagesDeleted();
	}

	@Override
	public Message[] dequeue(int max, int retryWaitSeconds) throws Exception
	{
		synchronized (connection)
		{
			dequeueStmt.setDate(1, new Date(new java.util.Date().getTime()));
			dequeueStmt.setInt(2, max);
			ArrayList<Message> messages = new ArrayList<Message>();

			// Load the first "max" messages available for dequeuing
			ResultSet resultSet = dequeueStmt.executeQuery();
			try
			{
				while (resultSet.next()) {
					Message message = readMessage(resultSet);
					messages.add(message);
				}
			}
			finally
			{
				resultSet.close();
			}

			// Update the NextRetry and Retry count
			Calendar nextRetry = Calendar.getInstance();
			nextRetry.add(Calendar.SECOND, retryWaitSeconds);
			for (Message message : messages) {
				setRetryStmt.setDate(1, new Date(nextRetry.getTime().getTime()));
				setRetryStmt.setString(2, message.id);
				setRetryStmt.execute();
			}
			connection.commit();

			Message[] messagesArray = (Message[]) messages.toArray(new Message[messages.size()]);
			onMessagesDequeued(messagesArray);
			return messagesArray;
		}
	}

	private Message readMessage(ResultSet resultSet) throws SQLException
	{
		Message message = new Message();
		message.id = resultSet.getString("Id");
		message.when = resultSet.getDate("When");
		message.from = resultSet.getString("From");
		message.to = resultSet.getString("To");
		message.text = resultSet.getString("Text");
		message.retries = resultSet.getInt("Retries");
		message.retryTime = resultSet.getDate("RetryTime");
		return message;
	}

	@Override
	public void enqueue(Message[] messages) throws Exception
	{
		synchronized (connection)
		{
			for (Message message : messages) {
				enqueueStmt.setString(1, message.id);
				enqueueStmt.setDate(2, new Date(message.when.getTime()));
				enqueueStmt.setString(3, message.from);
				enqueueStmt.setString(4, message.to);
				enqueueStmt.setString(5, message.text);
				enqueueStmt.execute();
			}

			connection.commit();
		}
		onMessagesEnqueued(messages);
	}

	@Override
	public void delete(String messageId) throws Exception
	{
		synchronized (connection)
		{
			deleteStmt.setString(1, messageId);
			deleteStmt.execute();
			connection.commit();
		}
		onMessagesDeleted();
	}

	@Override
	public Iterable<Message> iterateMessages()
	{
		return new Iterable<Message>()
		{
			@Override
			public Iterator<Message> iterator()
			{
				try {
					ArrayList<Message> messages = new ArrayList<Message>();

					synchronized (connection) {
						ResultSet resultSet = selectAllStmt.executeQuery();
						while (resultSet.next()) {
							Message message = readMessage(resultSet);
							messages.add(message);
						}
					}

					return messages.iterator();
				} catch (SQLException e) {
					throw new Error(e);
				}

			}
		};
	}
	
	@Override
	public int getMessageCount() throws Exception
	{
		synchronized (connection) {
			ResultSet resultSet = countStmt.executeQuery();
			try {
				resultSet.next();
				return resultSet.getInt(1);
			} finally {
				resultSet.close();
			}
		}
	}
	
	@Override
	public void force(String messageId) throws Exception
	{
		synchronized (connection) {
			forceStmt.setString(1, messageId);
			forceStmt.execute();
			connection.commit();
		}
	}
}
