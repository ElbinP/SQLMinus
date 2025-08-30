package org.misc.sqlminus;

public class SqlParser {

	public static class ProcRef {
		public final String schema;
		public final String name;
		public final boolean isFunction;

		ProcRef(String schema, String name, boolean isFunction) {
			this.schema = schema;
			this.name = name;
			this.isFunction = isFunction;
		}
	}

	/**
	 * Extract schema, name, and type (procedure/function) from a call string.
	 */
	public static ProcRef extractProcRef(String sql) {
		String clean = sql.trim();

		boolean isFunction = clean.startsWith("{?=");

		// Remove { }, and ?= call / call prefix
		clean = clean.replaceAll("[{}]", "");
		clean = clean.replaceFirst("(?i)^\\s*\\?=\\s*call\\s*", "");
		clean = clean.replaceFirst("(?i)^\\s*call\\s*", "");

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

		return new ProcRef(schema, name, isFunction);
	}
}
