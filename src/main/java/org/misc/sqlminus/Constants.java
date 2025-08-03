package org.misc.sqlminus;

public class Constants {
	public class PreferencesKeys {
		public static final String ROWS_TO_SELECT = "RowsToSelect";
		public static final String SQL_TEXTS = "SQLTexts";
		public static final String CONNECT_STRING = "ConnectString";
		public static final String DB_USERNAME = "DBUsername";
		public static final String DB_PASSWORD = "DBPassword";
		public static final String JARS_LIST = "JarsList";
		public static final String WINDOW_WIDTH = "WindowWidth";
		public static final String WINDOW_HEIGHT = "WindowHeight";
		public static final String WINDOW_X = "WindowX";
		public static final String WINDOW_Y = "WindowY";
		public static final String SQLFRAME_WINDOW_WIDTH = "SqlFrameWindowWidth";
		public static final String SQLFRAME_WINDOW_HEIGHT = "SqlFrameWindowHeight";
		public static final String SQLFRAME_WINDOW_X = "SqlFrameWindowX";
		public static final String SQLFRAME_WINDOW_Y = "SqlFrameWindowY";
		public static final String SQLFRAME_TOOLBAR_POSITION = "SqlFrameToolbarPosition";
		public static final String SQLFRAME_TOOLBAR_ORIENTATION = "SqlFrameToolbarOrientation";
		public static final String SQLFRAME_WORD_WRAP = "SqlFrameWordWrap";
		public static final String LOOK_AND_FEEL_CLASS = "LookAndFeelClass";
		public static final String SESSION_PREFIX = "Session-";
		public static final String MIN_COL_WIDTH = "MinColWidth";
		public static final String MAX_COL_WIDTH = "MaxColWidth";
		public static final String INTER_COLUMN_SPACING = "InterColumnSpacing";
		public static final String MAX_DATA_LENGTH = "MaxDataLength";
		public static final String NULL_REPRESENTATION = "NullRepresentation";
		public static final String ALWAYS_SHOW_ROW_DIVIDERS = "AlwaysShowRowDividers";
		public static final String CLEAR_SCREEN_BEFORE_EACH_RESULT = "ClearScreenBeforeEachResult";
		public static final String ENABLE_THREADS = "EnableThreads";
		public static final String OUTPUT_IN_GRID = "OutputInGrid";
	}

	public static final String SQL_HISTORY_FULL_FILE_PATH = System.getProperty("user.home")
			+ "/.org.misc.sqlminus/SQLHistory.xml.gz";
	public static final String PREFERENCES_SECRET_KEY_FILE = System.getProperty("user.home")
			+ "/.org.misc.sqlminus/sqlminus.secret";
}
