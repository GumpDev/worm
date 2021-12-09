package dev.gump;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WormQuery {
	private final Connection connection;
	private final PreparedStatement statement;
	private ResultSet resultSet = null;

	public WormQuery(Connection connection, PreparedStatement statement){
		this.connection = connection;
		this.statement = statement;
		try {
			this.resultSet = statement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void closeConnection(){
		try {
			this.connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public PreparedStatement getStatement() {
		return statement;
	}
}
