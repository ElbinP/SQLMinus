package org.misc.sqlminus;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CallableHelper {

	public static class OutParam {
		public final int position;
		public final String name;

		OutParam(int position, String name) {
			this.position = position;
			this.name = name;
		}
	}

	public static Map.Entry<CallableStatement, List<OutParam>> prepareWithOutParams(Connection conn,
			SqlParser.ProcRef ref, String sql) throws SQLException, SQLMinusException {

		CallableStatement cs = conn.prepareCall(sql);
		List<OutParam> outParams = new ArrayList<>();
		DatabaseMetaData meta = conn.getMetaData();

		// 1. Extract arguments inside parentheses
		int open = sql.indexOf('(');
		int close = sql.lastIndexOf(')');
		if (open < 0 || close < open) {
			throw new SQLException("Invalid SQL: cannot find argument list in " + sql);
		}
		String argsPart = sql.substring(open + 1, close);

		// Split on commas (naive but works if no commas inside literals)
		String[] args = argsPart.split(",");

		// Find positions of ? arguments (absolute position in call argument list)
		List<Integer> sqlOutPositions = new ArrayList<>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].trim().equals("?")) {
				sqlOutPositions.add(i + 1); // positions are 1-based
			}
		}

		// 2. Get procedure/function parameter metadata
		String procSchema = (ref.schema != null) ? ref.schema.toUpperCase(Locale.ROOT) : null;
		String procName = (ref.name != null) ? ref.name.toUpperCase(Locale.ROOT) : null;

		ResultSet rs = meta.getProcedureColumns(null, procSchema, procName, null);

		Map<String, List<Map<String, Object>>> overloads = new LinkedHashMap<>();
		try (rs) {
			while (rs.next()) {
				String specific = rs.getString("SPECIFIC_NAME");
				Map<String, Object> param = new LinkedHashMap<>();
				param.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
				param.put("COLUMN_TYPE", rs.getInt("COLUMN_TYPE"));
				param.put("DATA_TYPE", rs.getInt("DATA_TYPE"));
				param.put("ORDINAL_POSITION", rs.getInt("ORDINAL_POSITION"));

				overloads.computeIfAbsent(specific, k -> new ArrayList<>()).add(param);
			}
		}

		if (overloads.keySet().size() == 0) {
			throw new SQLMinusException("No metadata found for " + procSchema + "." + procName + " in database");
		}

		// 3. Find overload whose parameter count and OUT positions match the SQL
		List<Map<String, Object>> chosen = null;

		for (List<Map<String, Object>> params : overloads.values()) {
			// Sort params by ordinal position so they align with SQL order
			List<Map<String, Object>> sorted = params.stream()
					.sorted(Comparator.comparingInt(p -> (int) p.get("ORDINAL_POSITION"))).toList();

			if (sorted.size() != args.length) {
				continue; // parameter count mismatch
			}

			boolean matches = true;
			for (int i = 0; i < args.length; i++) {
				boolean isMarker = args[i].trim().equals("?");
				int colType = (int) sorted.get(i).get("COLUMN_TYPE");

				boolean isOut = colType == DatabaseMetaData.procedureColumnOut
						|| colType == DatabaseMetaData.procedureColumnInOut
						|| colType == DatabaseMetaData.procedureColumnReturn
						|| colType == DatabaseMetaData.functionColumnOut
						|| colType == DatabaseMetaData.functionColumnInOut
						|| colType == DatabaseMetaData.functionReturn;

				if (isMarker && !isOut) {
					matches = false; // SQL expected OUT, DB says IN
					break;
				}
				if (!isMarker && isOut) {
					matches = false; // SQL had literal, DB expected OUT
					break;
				}
			}

			if (matches) {
				chosen = sorted;
				break;
			}
		}

		if (chosen == null) {
			throw new SQLMinusException("No overload of " + procSchema + "." + procName + " matches argument pattern "
					+ Arrays.toString(args));
		}

		// 4. Register OUT/RETURN params in the order of ? placeholders
		List<Map<String, Object>> outs = chosen.stream().filter(p -> {
			int colType = (int) p.get("COLUMN_TYPE");
			return colType == DatabaseMetaData.procedureColumnOut || colType == DatabaseMetaData.procedureColumnInOut
					|| colType == DatabaseMetaData.procedureColumnReturn
					|| colType == DatabaseMetaData.functionColumnOut || colType == DatabaseMetaData.functionColumnInOut
					|| colType == DatabaseMetaData.functionReturn;
		}).sorted(Comparator.comparingInt(p -> (int) p.get("ORDINAL_POSITION"))).toList();

		int jdbcIndex = 1; // JDBC indexes are sequential for the ? markers

		if (ref.hasReturn) {
			cs.registerOutParameter(jdbcIndex, Types.VARCHAR);
			outParams.add(new OutParam(jdbcIndex, "RETURN VALUE"));
			jdbcIndex++;
		}

		for (Map<String, Object> p : outs) {
			int dataType = (int) p.get("DATA_TYPE");
			String name = (String) p.get("COLUMN_NAME");

			cs.registerOutParameter(jdbcIndex, dataType);
			outParams.add(new OutParam(jdbcIndex, name));
			jdbcIndex++;
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
			Object val;
			try {
				val = cs.getObject(p.position);
			} catch (SQLException e) {
				val = "";
			}
			values.put(p.name != null ? p.name : ("param" + p.position), val);
		}
		return values;
	}
}
