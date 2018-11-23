package org.misc.sqlminus;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import nocom.special.CustomizedMouseAdapter;

public class CellContentsViewFrame extends JFrame {

	private JTextArea textArea;
	private CustomizedMouseAdapter mouseAdapter;
	private JScrollPane scrollPane;

	public CellContentsViewFrame(Color backgroundColor) {
		super("Cell contents");
		textArea = new JTextArea();
		textArea.setBackground(backgroundColor);
		textArea.setEditable(false);
		mouseAdapter = new CustomizedMouseAdapter(false);
		textArea.addMouseListener(mouseAdapter);
		scrollPane = new JScrollPane(textArea);
		add(scrollPane);
	}

	public JPopupMenu getContentsViewPopupMenu() {
		return mouseAdapter.getPopupMenu();
	}

	public void setContent(String content) {
		textArea.setText(content);
	}

	public JScrollPane getContentsViewArea() {
		return scrollPane;
	}

	public JTextArea getTextArea() {
		return textArea;
	}

}
