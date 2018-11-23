package org.misc.sqlminus;

import java.awt.Font;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceLoader {

	private static ResourceBundle resources;

	static {
		try {
			resources = ResourceBundle.getBundle("resources.SQLMinus", Locale.getDefault());
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public static String getResourceString(String key) {
		String str = "";
		try {
			str = resources.getString(key);
		} catch (Exception e) {
			System.err.println(e);
		}
		return str;
	}

	public static int getFontStyle(String key) {
		String str = getResourceString(key);
		int fontStyle = Font.PLAIN;
		if (str.equals("PLAIN|ITALIC")) {
			fontStyle = Font.PLAIN | Font.ITALIC;
		} else if (str.equals("BOLD")) {
			fontStyle = Font.BOLD;
		} else if (str.equals("BOLD|ITALIC")) {
			fontStyle = Font.BOLD | Font.ITALIC;
		}
		return fontStyle;
	}

	public static int getFontSize(String key) {
		String str = getResourceString(key);
		int fSize = 14;
		try {
			fSize = Integer.parseInt(str);
		} catch (Exception e) {
		}
		return fSize;
	}

}
