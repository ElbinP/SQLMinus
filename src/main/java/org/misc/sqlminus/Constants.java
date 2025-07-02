package org.misc.sqlminus;

public class Constants {
	public class PreferencesKeys {
		public static final String ROWS_TO_SELECT = "RowsToSelect";
		public static final String SQL_TEXTS = "SQLTexts";
		public static final String DRIVER_CLASSNAME = "DriverClassname";
		public static final String CONNECT_STRING = "ConnectString";
		public static final String DB_USERNAME = "DBUsername";
		public static final String DB_PASSWORD = "DBPassword";
		public static final String JARS_LIST = "JarsList";
		public static final String WINDOW_WIDTH = "WindowWidth";
		public static final String WINDOW_HEIGHT = "WindowHeight";
		public static final String WINDOW_X = "WindowX";
		public static final String WINDOW_Y = "WindowY";
	}

	public static final String SQL_HISTORY_FULL_FILE_PATH = System.getProperty("user.home")
			+ "/.org.misc.sqlminus/SQLHistory.xml.gz";
	public static final String PREFERENCES_SECRET_KEY_FILE = System.getProperty("user.home")
			+ "/.org.misc.sqlminus/sqlminus.secret";
}
