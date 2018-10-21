package org.misc.sqlminus;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import nocom.special.CustomizedMouseAdapter;

public class CellContentsViewFrame extends JFrame {

	private JTextArea textArea;
	private CustomizedMouseAdapter popupMenu;

	public CellContentsViewFrame() {
		textArea = new JTextArea();
		popupMenu = new CustomizedMouseAdapter(false);
		textArea.addMouseListener(popupMenu);
		JScrollPane scrollPane = new JScrollPane(textArea);
		add(scrollPane);
	}

	public CustomizedMouseAdapter getContentsViewPopupMenu() {
		return popupMenu;
	}

	public void setContent(String content) {
		textArea.setText(content);
	}

}
