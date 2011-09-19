package org.instedd.mobilegw.messaging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbLastIdStore implements LastIdStore
{
	private Connection connection;
	private PreparedStatement insertLastIdStmt;
	private PreparedStatement updateLastIdStmt;
	private PreparedStatement selectLastIdStmt;

	public DbLastIdStore(Connection connection)
	{
		try {
			this.connection = connection;
			ensureTable();

			insertLastIdStmt = connection.prepareStatement("INSERT INTO LastId (Id) VALUES (?)");
			updateLastIdStmt = connection.prepareStatement("UPDATE LastId SET Id = ?");
			selectLastIdStmt = connection.prepareStatement("SELECT Id FROM LastId");

		} catch (SQLException e) {
			throw new Error(e);
		}
	}

	private void ensureTable() throws SQLException
	{
		ResultSet tables = connection.getMetaData().getTables(null, null, "LastId", null);
		try {
			if (!tables.next()) {
				Statement stmt = connection.createStatement();
				stmt.execute("CREATE TABLE LastId ([Id] TEXT NOT NULL)");
				connection.commit();
			}
		} finally {
			tables.close();
		}
	}

	@Override
	public String getLastId()
	{
		synchronized (connection)
		{
			try {
				ResultSet resultSet = selectLastIdStmt.executeQuery();
				try {
					if (!resultSet.next())
						return null;
					return resultSet.getString("Id");
				} finally {
					resultSet.close();
				}

			} catch (SQLException e) {
				throw new Error(e);
			}
		}
	}

	@Override
	public void setLastId(String id)
	{
		synchronized (connection)
		{
			try {
				if (getLastId() == null) {
					insertLastIdStmt.setString(1, id);
					insertLastIdStmt.execute();
				} else {
					updateLastIdStmt.setString(1, id);
					updateLastIdStmt.execute();
				}
			} catch (Exception e) {
				throw new Error(e);
			}
		}
	}

}
