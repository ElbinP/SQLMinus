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

		// CallableStatement detection (JDBC escape syntax)
		if (trimmed.startsWith("{call") || trimmed.startsWith("call") || trimmed.startsWith("{?=")
				|| trimmed.startsWith("{? =")) {
			SqlParser.ProcRef ref = SqlParser.extractProcRef(sql);
			var entry = CallableHelper.prepareWithOutParams(conn, ref, sql);
			CallableStatement cs = entry.getKey();
			List<CallableHelper.OutParam> outParams = entry.getValue();
			return new StatementWithOutParams(cs, Optional.of(outParams));
		}

		// Otherwise plain Statement
		return new StatementWithOutParams(conn.createStatement(), Optional.empty());
	}

	public static record StatementWithOutParams(Statement statement,
			Optional<List<CallableHelper.OutParam>> outParams) {

	}
}
