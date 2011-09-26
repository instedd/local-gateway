package org.instedd.mobilegw.messaging;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.instedd.mobilegw.messaging.DirectedMessage.Direction;

public class DbDirectedMessageStore implements DirectedMessageStore {

	private Collection<DirectedMessageStoreListener> listeners = new LinkedList<DirectedMessageStoreListener>();
	
	private Connection connection;
	private final String table;	
	
	private PreparedStatement addStmt;
	private PreparedStatement selectAllStmt;
	private PreparedStatement selectPhoneStmt;
	private PreparedStatement deletePhoneStmt;

	public DbDirectedMessageStore(Connection connection, String table)
	{
		this.table = table;
		this.connection = connection;
		
		try {			
			ensureTable();

			addStmt = connection.prepareStatement("INSERT INTO " + table + " (Id, [When], [From], [To], [Text], [Direction]) VALUES (?,?,?,?,?,?)");
			selectAllStmt = connection.prepareStatement("SELECT * FROM " + table + " ORDER BY [When]");
			selectPhoneStmt = connection.prepareStatement("SELECT * FROM " + table + " WHERE ([From] = ? AND [Direction] = 'AT') OR ([To] = ? AND [Direction] = 'AO') ORDER BY [When]");	
			deletePhoneStmt = connection.prepareStatement("DELETE FROM " + table + " WHERE ([From] = ? AND [Direction] = 'AT') OR ([To] = ? AND [Direction] = 'AO')");
		} catch (SQLException e) {
			throw new Error(e);
		}
	}

	@Override
	public void addDirectedMessageStoreListener(
			DirectedMessageStoreListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeDirectedMessageStoreListener(
			DirectedMessageStoreListener listener) {
		this.listeners.remove(listener);		
	}

	@Override
	public void addMessage(DirectedMessage message) throws Exception {
		synchronized (connection) {
			addStmt.setString(1, message.id);
			addStmt.setDate(2, new Date(message.when.getTime()));
			addStmt.setString(3, message.from);
			addStmt.setString(4, message.to);
			addStmt.setString(5, message.text);
			addStmt.setString(6, message.direction.name());
			addStmt.execute();
			connection.commit();
		} onMessageAdded(message);
	}

	@Override
	public void deleteMessages(String phone) throws Exception {
		synchronized (connection) {
			deletePhoneStmt.setString(1, phone);
			deletePhoneStmt.setString(2, phone);
			deletePhoneStmt.execute();
			connection.commit();
		}
	}

	@Override
	public Iterable<DirectedMessage> iterateMessages() {
		return iterateMessages(null);
	}

	@Override
	public Iterable<DirectedMessage> iterateMessages(final String phone) {
		return new Iterable<DirectedMessage>()
		{
			@Override
			public Iterator<DirectedMessage> iterator()
			{
				try {
					ArrayList<DirectedMessage> messages = new ArrayList<DirectedMessage>();

					synchronized (connection) {
						ResultSet resultSet;
						if (phone == null) {
							resultSet = selectAllStmt.executeQuery();
						} else {
							selectPhoneStmt.setString(1, phone);
							selectPhoneStmt.setString(2, phone);
							resultSet = selectPhoneStmt.executeQuery();
						}
						
						while (resultSet.next()) {
							DirectedMessage message = readMessage(resultSet);
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
	
	private DirectedMessage readMessage(ResultSet resultSet) throws SQLException {
		DirectedMessage message = new DirectedMessage();
		message.id = resultSet.getString("Id");
		message.when = resultSet.getDate("When");
		message.from = resultSet.getString("From");
		message.to = resultSet.getString("To");
		message.text = resultSet.getString("Text");
		message.direction = Direction.valueOf(resultSet.getString("Direction"));
		return message;
	}

	private void onMessageAdded(DirectedMessage message) {
		// Copy list locally to avoid concurrent modification exceptions
		List<DirectedMessageStoreListener> listeners = new ArrayList<DirectedMessageStoreListener>(this.listeners);
		for(DirectedMessageStoreListener listener : listeners) {
			listener.messageAdded(message);
		}
	}

	private void ensureTable() throws SQLException {
		ResultSet tables = connection.getMetaData().getTables(null, null, table, null);
		try {
			if (!tables.next()) {
				Statement stmt = connection.createStatement();
				stmt.execute("CREATE TABLE " + table + " (\n" + 
						"[OrderId] INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
						"[Id] TEXT UNIQUE NOT NULL,\n" +
						"[When] INTEGER NOT NULL,\n" +
						"[From] TEXT NOT NULL,\n" +
						"[To] TEXT NOT NULL,\n" +
						"[Text] TEXT NOT NULL,\n" + 
						"[Direction] TEXT NOT NULL)");
				connection.commit();
			}
		} finally {
			tables.close();
		}
	}

}
