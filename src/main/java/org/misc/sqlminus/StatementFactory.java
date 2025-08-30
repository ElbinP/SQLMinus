package org.misc.sqlminus;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementFactory {

	public static Statement getStatement(Connection conn, String sql) throws SQLException {
		String trimmed = sql.trim();

		// CallableStatement detection (JDBC escape syntax)
		if (trimmed.startsWith("{call") || trimmed.startsWith("call") || trimmed.startsWith("{?=")
				|| trimmed.startsWith("{? =")) {
			return conn.prepareCall(sql);
		}

		// Otherwise plain Statement
		return conn.createStatement();
	}
}
