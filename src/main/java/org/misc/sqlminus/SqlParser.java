package org.misc.sqlminus;

public class SqlParser {

	public static class ProcRef {
		public final String schema;
		public final String name;
		public final boolean hasReturn; // true if { ? = call ... } form

		ProcRef(String schema, String name, boolean hasReturn) {
			this.schema = schema;
			this.name = name;
			this.hasReturn = hasReturn;
		}
	}

	/**
	 * Extract schema, name, and type (procedure/function) from a call string.
	 */
	public static ProcRef extractProcRef(String sql) {
		String clean = sql.trim();

		// Detect return placeholder
		boolean hasReturn = clean.matches("^\\{?\\s*\\?\\s*=.*");

		// Remove { }
		clean = clean.replaceAll("[{}]", "").trim();

		// Remove ?= call or call prefix (case-insensitive)
		clean = clean.replaceFirst("(?i)^\\?\\s*=\\s*call\\s*", "");
		clean = clean.replaceFirst("(?i)^call\\s*", "");

		// Cut off args (...)
		int parenIdx = clean.indexOf('(');
		if (parenIdx > 0) {
			clean = clean.substring(0, parenIdx);
		}

		clean = clean.trim();

		// Split schema.proc
		String schema = null;
		String name = clean;
		int dotIdx = clean.indexOf('.');
		if (dotIdx > 0) {
			schema = clean.substring(0, dotIdx).trim();
			name = clean.substring(dotIdx + 1).trim();
		}

		return new ProcRef(schema, name, hasReturn);
	}

}
