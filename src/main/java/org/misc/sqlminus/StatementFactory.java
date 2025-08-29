package org.misc.sqlminus;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class StatementFactory {

	/**
	 * Returns the appropriate Statement object based on the SQL.
	 *
	 * @param conn JDBC connection
	 * @param sql  SQL string (SELECT/UPDATE/INSERT, {call ...}, {?= call ...},
	 *             etc.)
	 * @return Statement (could be Statement, PreparedStatement, or
	 *         CallableStatement)
	 * @throws SQLException if SQL preparation fails
	 */
	public static Statement getStatement(Connection conn, String sql) throws SQLException {
		String trimmed = sql.trim();

		// CallableStatement detection (JDBC escape syntax)
		if (trimmed.startsWith("{call") || trimmed.startsWith("{?=") || trimmed.startsWith("{? =")) {
			return conn.prepareCall(sql);
		}

		// PreparedStatement heuristic: contains '?' placeholders
		if (trimmed.contains("?")) {
			return conn.prepareStatement(sql);
		}

		// Otherwise plain Statement
		return conn.createStatement();
	}
}
