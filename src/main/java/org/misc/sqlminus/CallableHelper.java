package org.misc.sqlminus;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CallableHelper {

	public static class OutParam {
		public final int position;
		public final String name;
		public final int sqlType;

		OutParam(int position, String name, int sqlType) {
			this.position = position;
			this.name = name;
			this.sqlType = sqlType;
		}
	}

	/**
	 * Prepares a CallableStatement and registers OUT/RETURN params automatically.
	 * Works for both procedures and functions.
	 */
	public static Map.Entry<CallableStatement, List<OutParam>> prepareWithOutParams(Connection conn,
			SqlParser.ProcRef ref, String sql) throws SQLException {

		CallableStatement cs = conn.prepareCall(sql);
		List<OutParam> outParams = new ArrayList<>();
		DatabaseMetaData meta = conn.getMetaData();

		ResultSet rs;
		if (ref.isFunction) {
			rs = meta.getFunctionColumns(null, ref.schema, ref.name, null);
		} else {
			rs = meta.getProcedureColumns(null, ref.schema, ref.name, null);
		}

		try (rs) {
			while (rs.next()) {
				String paramName = rs.getString("COLUMN_NAME");
				int columnType = rs.getInt("COLUMN_TYPE"); // IN, OUT, INOUT, RETURN
				int dataType = rs.getInt("DATA_TYPE"); // java.sql.Types
				int position = rs.getInt("ORDINAL_POSITION");

				// For functions, RETURN value is usually position 0
				if (columnType == DatabaseMetaData.procedureColumnOut
						|| columnType == DatabaseMetaData.procedureColumnInOut
						|| columnType == DatabaseMetaData.procedureColumnReturn
						|| columnType == DatabaseMetaData.functionColumnOut
						|| columnType == DatabaseMetaData.functionColumnInOut
						|| columnType == DatabaseMetaData.functionReturn) {

					cs.registerOutParameter(position, dataType);
					outParams.add(new OutParam(position, paramName, dataType));
				}
			}
		}

		return new AbstractMap.SimpleEntry<>(cs, outParams);
	}

	/**
	 * Reads OUT params dynamically into a map.
	 */
	public static Map<String, Object> readOutParams(CallableStatement cs, List<OutParam> outParams)
			throws SQLException {

		Map<String, Object> values = new LinkedHashMap<>();
		for (OutParam p : outParams) {
			Object val = cs.getObject(p.position);
			values.put(p.name != null ? p.name : ("param" + p.position), val);
		}
		return values;
	}
}
