package org.misc.sqlminus;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class StatementFactory {

	public static StatementWithOutParams getStatement(Connection conn, String sql)
			throws SQLException, SQLMinusException {

		String trimmed = sql.trim();

		// Detect if this is a CALL (procedure or function)
		boolean isCall = trimmed.matches("(?i)^\\{?\\s*\\??\\s*=?.*call\\s+.*\\}?$");

		if (isCall) {
			// Extract procedure reference
			SqlParser.ProcRef ref = SqlParser.extractProcRef(sql);

			// Detect if the call has OUT / RETURN parameters
			int open = trimmed.indexOf('(');
			int close = trimmed.lastIndexOf(')');
			boolean hasOutPlaceholder = false;
			if (open >= 0 && close > open) {
				String args = trimmed.substring(open + 1, close);
				if (args.contains("?")) {
					hasOutPlaceholder = true;
				}
			}

			if (hasOutPlaceholder) {
				// Must use CallableStatement
				var entry = CallableHelper.prepareWithOutParams(conn, ref, sql);
				CallableStatement cs = entry.getKey();
				List<CallableHelper.OutParam> outParams = entry.getValue();
				return new StatementWithOutParams(cs, Optional.of(outParams));
			} else {
				// Optional optimization: call with literals only can use Statement
				return new StatementWithOutParams(conn.createStatement(), Optional.empty());
			}
		}

		// Otherwise plain Statement
		return new StatementWithOutParams(conn.createStatement(), Optional.empty());
	}

	public static record StatementWithOutParams(Statement statement,
			Optional<List<CallableHelper.OutParam>> outParams) {
	}
}
