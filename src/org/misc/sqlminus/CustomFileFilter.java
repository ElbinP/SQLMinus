package org.misc.sqlminus;

import java.io.File;

public class CustomFileFilter extends javax.swing.filechooser.FileFilter {

	public boolean accept(File file) {
		if (file.isDirectory())
			return true;
		String ext = getExtension(file);
		if (ext != null) {
			if (ext.equals("jar") || ext.equals("zip")) {
				return true;
			}
		}
		return false;
	}

	public String getDescription() {
		return "Jar / Zip Files";
	}

	public String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

}
