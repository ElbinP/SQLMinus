package nocom.special;

import java.io.File;
import java.util.Vector;

public class UtilityFunctions {

	public UtilityFunctions() {
	}

	/**
	 * Splits up the string <code>data</code> into elements of size
	 * <code>elementlength</code> and returns it as a vector with its elements in
	 * the correct order.<br>
	 * Throws an IllegalArgumentException if elementlength is less than 1.
	 */
	public static Vector splitUpString(String data, int elementlength) {
		if (elementlength < 1) {
			throw new IllegalArgumentException("elementlength cannot be less than 1");
		}
		Vector splitdata = new Vector();
		if (data.length() <= elementlength) {
			splitdata.addElement(data);
		} else {
			while (data.length() > elementlength) {
				splitdata.addElement(data.substring(0, elementlength));
				data = data.substring(elementlength);
			}
			if (data.length() > 0) {
				splitdata.addElement(data);
			}
		}
		return splitdata;
	}

	/**
	 * Returns the length of the longest string in the array of strings. Returns 0
	 * if <code>data</code> is null.
	 */
	public static int getMaxLength(String[] data) {
		int maxlength = 0;
		for (int i = 0; i < data.length; i++) {
			try {
				if (maxlength < data[i].length()) {
					maxlength = data[i].length();
				}
			} catch (NullPointerException ne) {
				// NullPointerException maybe thrown when data[i] is null.
			}
		}
		return maxlength;
	}

	/**
	 * Returns the longest string in the array of strings. Returns an empty string
	 * if <code>data</code> is null.
	 */
	public static String getMaxLengthString(String[] data) {
		String maxString = "";
		for (int i = 0; i < data.length; i++) {
			try {
				if (maxString.length() < data[i].length()) {
					maxString = data[i];
				}
			} catch (NullPointerException ne) {
				// NullPointerException maybe thrown when data[i] is null.
			}
		}
		return maxString;
	}

	/**
	 * Returns the longest string representation in the array of numbers. Returns an
	 * empty string if <code>data</code> is null.
	 */
	public static String getMaxLengthString(Number[] data) {
		String maxString = "";
		for (int i = 0; i < data.length; i++) {
			try {
				if (maxString.length() < Double.toString(data[i].doubleValue()).length()) {
					maxString = Double.toString(data[i].doubleValue());
				}
			} catch (NullPointerException ne) {
				// NullPointerException maybe thrown when data[i] is null.
			}
		}
		return maxString;
	}

	/**
	 * Returns the longest string representation in the array of objects. Returns an
	 * empty string if <code>data</code> is null.
	 */
	public static String getMaxLengthString(Object[] data) {
		String maxString = "";
		for (int i = 0; i < data.length; i++) {
			try {
				if (maxString.length() < data[i].toString().length()) {
					maxString = data[i].toString();
				}
			} catch (NullPointerException ne) {
				// NullPointerException maybe thrown when data[i] is null.
			}
		}
		return maxString;
	}

	/**
	 * If the length of <code>data</code> is greater than length, then data is
	 * reduced to the first <code>length</code> characters, the word "..." is
	 * appended to it and it is returned. Else the same data is returned.
	 */
	public static String truncateString(String data, int length) {
		if (data.length() > length) {
			data = data.substring(0, length) + "...";
		}
		return data;
	}

	/**
	 * Returns all the files within <code>dir</code>. If <code>useFileHiding</code>
	 * is true then hidden files are not returned, else hidden files are also
	 * returned.
	 */
	public static File[] getFilesInDirectory(File dir, boolean useFileHiding) {
		Vector files = new Vector();
		File[] names = dir.listFiles();
		File f;
		int nameCount = names == null ? 0 : names.length;
		for (int i = 0; i < nameCount; i++) {
			f = names[i];
			if (f.isFile() || f.isDirectory()) {
				if (!useFileHiding || !f.isHidden()) {
					files.addElement(f);
				}
			}
		}
		return (File[]) files.toArray(new File[files.size()]);
	}

	/*
	 * Convert a filesize into human readable form. Converts <code>size<code> into
	 * the form 21.9MB,10KB etc.
	 */
	public static String sensibleFileSize(long size) {
		String retval;
		String qt, rt;
		if (size > (1024L * 1024L)) {
			qt = size / (1024L * 1024L) + "";
			rt = size % (1024L * 1024L) + "";
			retval = qt + "." + rt.substring(0, 1) + " MB";
		} else if (size > 1024L) {
			qt = size / 1024L + "";
			rt = size % 1024L + "";
			retval = qt + "." + rt.substring(0, 1) + " KB";
		} else {
			retval = size + " B";
		}
		return retval;
	}

	/***
	 * Get error messages from all inner exceptions
	 * 
	 * @param e
	 * @return
	 */
	public static String getExceptionMessagesWithClassNames(Throwable e) {
		StringBuilder sb = new StringBuilder();
		while (e != null) {
			sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");
			e = e.getCause();
		}
		return sb.toString();
	}

	/***
	 * Get error messages from all inner exceptions
	 * 
	 * @param e
	 * @return
	 */
	public static String getExceptionMessages(Throwable e) {
		StringBuilder sb = new StringBuilder();
		while (e != null) {
			sb.append(e.getMessage()).append("\n");
			e = e.getCause();
		}
		return sb.toString();
	}

}
