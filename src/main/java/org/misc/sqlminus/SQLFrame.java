package org.misc.sqlminus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.misc.sqlminus.sqlhistory.SQLHistoryHelper;

import nocom.special.ImageReader;
import nocom.special.IndexedVector;
import nocom.special.UndoableTextArea;
import nocom.special.VectorIndexOutOfBoundsException;

public class SQLFrame extends JFrame implements ActionListener, DocumentListener, ChangeListener, FocusListener {

	private UndoableTextArea textInput;
	private IndexedVector sqlCommands;
	private SQLMinus sqlMinusObject;
	private JTextArea lineNumberPanel;
	private JButton back, forward, undo, redo, execute, save, open, clearHistory, deleteHistoryEntryButton;
	private JPopupMenu popup;
	private JFileChooser fileChooser;
	private JScrollPane textSPane, lineSPane;
	private static final int HISTORY_CHARACTER_WIDTH = 23;
	private JPanel centerPanel;
	private String lineSeparator;
	private FileSaverThread fileSaver;
	private FileOpenerThread fileOpener;
	private LineNumberSetter lineNumberSetter;
	private final SQLMinusPreferences sqlMinusPreferences;
	private final DefaultListModel<String> historyModel = new DefaultListModel<String>();
	private final JList<String> historyList;

	public SQLFrame(final SQLMinus sqlMinusObject, Font tfont, Font f, Color backgroundLight, int hgap, int vgap,
			SQLMinusPreferences sqlMinusPreferences) {
		super("Enter SQL Statement");
		this.sqlMinusPreferences = sqlMinusPreferences;

		getContentPane().setLayout(new BorderLayout(hgap, vgap));
		this.sqlMinusObject = sqlMinusObject;
		textInput = new UndoableTextArea();
		textInput.setBorder(BorderFactory.createLoweredBevelBorder());
		sqlCommands = new IndexedVector();
		fileChooser = new JFileChooser(System.getProperty("user.home"));
		sqlMinusObject.laf.addComponentToMonitor(fileChooser);
		lineSeparator = System.getProperty("line.separator");
		fileSaver = new FileSaverThread();
		fileOpener = new FileOpenerThread();
		lineNumberSetter = new LineNumberSetter();

		lineNumberPanel = new JTextArea("1  " + lineSeparator);
		lineNumberPanel.setFont(tfont);
		lineNumberPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		lineNumberPanel.setEditable(false);
		lineNumberPanel.setBackground(null);

		JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);

		try {
			back = new JButton(new ImageIcon(ImageReader.getImage(this.getClass(), "/images/back.gif")));
		} catch (Exception e) {
			back = new JButton("Back");
		}
		try {
			forward = new JButton(new ImageIcon(ImageReader.getImage(this.getClass(), "/images/forward.gif")));
		} catch (Exception e) {
			forward = new JButton("Forward");
		}
		try {
			deleteHistoryEntryButton = new JButton(
					new ImageIcon(ImageReader.getImage(this.getClass(), "/images/delete.png")));
		} catch (Exception e) {
			deleteHistoryEntryButton = new JButton("Delete");
		}
		try {
			undo = new JButton(new ImageIcon(ImageReader.getImage(this.getClass(), "/images/undo.gif")));
		} catch (Exception e) {
			undo = new JButton("Undo");
		}
		try {
			redo = new JButton(new ImageIcon(ImageReader.getImage(this.getClass(), "/images/redo.gif")));
		} catch (Exception e) {
			redo = new JButton("Redo");
		}
		try {
			execute = new JButton(new ImageIcon(ImageReader.getImage(this.getClass(), "/images/execute.gif")));
		} catch (Exception e) {
			execute = new JButton("Execute");
		}
		try {
			open = new JButton(new ImageIcon(ImageReader.getImage(this.getClass(), "/images/open.gif")));
		} catch (Exception e) {
			open = new JButton("Open");
		}
		try {
			save = new JButton(new ImageIcon(ImageReader.getImage(this.getClass(), "/images/save.gif")));
		} catch (Exception e) {
			save = new JButton("Save");
		}
		try {
			clearHistory = new JButton(new ImageIcon(ImageReader.getImage(this.getClass(), "/images/history.gif")));
		} catch (Exception e) {
			clearHistory = new JButton("Empty");
		}

		back.setToolTipText("Back");
		// back.setFont(f);
		back.setActionCommand("Back");
		back.addActionListener(this);

		forward.setToolTipText("Forward");
		// forward.setFont(f);
		forward.setActionCommand("Forward");
		forward.addActionListener(this);

		deleteHistoryEntryButton.setToolTipText("Delete history entry");
		deleteHistoryEntryButton.setActionCommand("Delete history entry");
		deleteHistoryEntryButton.addActionListener(this);

		undo.setToolTipText("Undo");
		// undo.setFont(f);
		undo.setActionCommand("Undo");
		undo.addActionListener(this);

		redo.setToolTipText("Redo");
		// redo.setFont(f);
		redo.setActionCommand("Redo");
		redo.addActionListener(this);

		execute.setToolTipText("Execute");
		// execute.setFont(f);
		execute.setActionCommand("Execute");
		execute.addActionListener(this);

		open.setToolTipText("Open a file that contains a query");
		// open.setFont(f);
		open.setActionCommand("Open");
		open.addActionListener(this);

		save.setToolTipText("Save query to a file");
		// save.setFont(f);
		save.setActionCommand("Save");
		save.addActionListener(this);

		clearHistory.setToolTipText("Clear history");
		// clearHistory.setFont(f);
		clearHistory.setActionCommand("Empty");
		clearHistory.addActionListener(this);

		toolBar.add(open);
		toolBar.add(save);
		toolBar.addSeparator();
		toolBar.add(back);
		toolBar.add(forward);
		toolBar.add(deleteHistoryEntryButton);
		toolBar.add(undo);
		toolBar.add(redo);
		toolBar.addSeparator();
		toolBar.add(execute);
		toolBar.add(clearHistory);

		getContentPane().add(toolBar, BorderLayout.NORTH);

		List<String> sqlHistory = null;
		try {
			sqlHistory = SQLHistoryHelper.getSQLCommandsFromHistory(sqlMinusPreferences);
			if (sqlHistory.size() > 0) {
				sqlCommands.removeAllElements();
				sqlCommands.addAll(sqlHistory);
				textInput.setText(sqlCommands.get(0));
			}
		} catch (Exception e) {
			sqlMinusObject.popMessage("Error loading SQL History. " + e.getMessage());
		}

		updateToolBarButtons();
		textInput.setFont(tfont);
		textInput.setToolTipText("Ctrl+E to execute");
		textInput.setBackground(backgroundLight);
		textInput.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent ke) {
				try {
					if (ke.isControlDown() && (ke.getKeyCode() == ke.VK_E)) {
						execute();
					} else if (ke.isControlDown() && (ke.getKeyCode() == ke.VK_UP)) {
						goForwardInHistory();
					} else if (ke.isControlDown() && (ke.getKeyCode() == ke.VK_DOWN)) {
						goBackInHistory();
					} else if (ke.isControlDown() && (ke.getKeyCode() == ke.VK_Z)) {
						textInput.undo();
					} else if (ke.isControlDown() && (ke.getKeyCode() == ke.VK_Y)) {
						textInput.redo();
					} else if (ke.isControlDown() && (ke.getKeyCode() == ke.VK_O)) {
						openFile();
					} else if (ke.isControlDown() && (ke.getKeyCode() == ke.VK_S)) {
						saveToFile();
					} else if (ke.isControlDown() && (ke.getKeyCode() == ke.VK_U)) {
						sqlCommands.insertString(textInput.getText());
						saveSQLCommandsToHistoryFile();
					}
				} catch (VectorIndexOutOfBoundsException ve) {
					Toolkit.getDefaultToolkit().beep();
					// System.err.println(ve);
				} catch (CannotUndoException e) {
					Toolkit.getDefaultToolkit().beep();
					// System.err.println(e);
				} catch (CannotRedoException e) {
					Toolkit.getDefaultToolkit().beep();
					// System.err.println(e);
				} finally {
					updateToolBarButtons();
				}
			}

			public void keyTyped(KeyEvent ke) {
				updateToolBarButtons();
			}

			public void keyReleased(KeyEvent ke) {
				updateToolBarButtons();
			}
		});
		// textInput.addMouseListener(sqlMinusObject.commonAdapter);
		JMenuItem cut = new JMenuItem("Cut");
		JMenuItem copy = new JMenuItem("Copy");
		JMenuItem paste = new JMenuItem("Paste");
		JMenuItem selectAll = new JMenuItem("Select All");
		JMenuItem clear = new JMenuItem("Clear");

		cut.addActionListener(this);
		copy.addActionListener(this);
		paste.addActionListener(this);
		selectAll.addActionListener(this);
		clear.addActionListener(this);

		popup = new JPopupMenu();
		popup.add(cut);
		popup.add(copy);
		popup.add(paste);
		popup.add(selectAll);
		popup.addSeparator();
		popup.add(clear);
		sqlMinusObject.laf.addComponentToMonitor(popup);

		textInput.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				if (me.getModifiers() == me.BUTTON3_MASK) {
					popup.show(me.getComponent(), me.getX(), me.getY());
				}
			}
		});

		textInput.getDocument().addDocumentListener(this);
		textInput.setTabSize(4);

		textSPane = new JScrollPane(textInput);
		textSPane.getViewport().addChangeListener(this);
		lineSPane = new JScrollPane(lineNumberPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(textSPane, BorderLayout.CENTER);
		centerPanel.add(lineSPane, BorderLayout.WEST);

		historyList = new JList<String>(historyModel);
		historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane historyScrollPane = new JScrollPane(historyList);
		historyScrollPane.setBorder(BorderFactory.createTitledBorder("History"));
		historyList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)
						&& historyList.getSelectedIndex() != -1) {
					try {
						textInput.setText(sqlCommands.get(historyList.getSelectedIndex() + 1));
						sqlCommands.setSelectedIndex(historyList.getSelectedIndex() + 1);
						updateToolBarButtons();
						SwingUtilities.invokeLater(lineNumberSetter);
					} catch (VectorIndexOutOfBoundsException e1) {
						Toolkit.getDefaultToolkit().beep();
					}
				}
			}
		});
		reloadHistoryPanel();
		getContentPane().add(historyScrollPane, BorderLayout.EAST);

		this.addFocusListener(this);

		getContentPane().add(centerPanel, BorderLayout.CENTER);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				popup.setVisible(false);
				sqlMinusObject.showSQLFrame(false);
			}
		});

	}

	private void reloadHistoryPanel() {
		Thread historyReloadThread = new Thread(() -> {
			historyModel.clear();
			for (int i = 1; i < sqlCommands.size(); i++) {
				historyModel.addElement(getFirstFewChars(sqlCommands.get(i)));
			}
		});
		SwingUtilities.invokeLater(historyReloadThread);
	}

	private String getFirstFewChars(String multilineText) {
		String[] lines = multilineText.split("\\R");

		for (String line : lines) {
			String trimmed = line.trim();
			if (!trimmed.isEmpty()) {
				if (trimmed.length() > HISTORY_CHARACTER_WIDTH - 3) {
					return trimmed.substring(0, HISTORY_CHARACTER_WIDTH - 3) + "...";
				} else {
					return String.format("%-" + HISTORY_CHARACTER_WIDTH + "s", trimmed);
				}
			}
		}

		return " ".repeat(HISTORY_CHARACTER_WIDTH); // all lines were empty
	}

	private void updateToolBarButtons() {
		undo.setEnabled(textInput.canUndo());
		redo.setEnabled(textInput.canRedo());
		forward.setEnabled(sqlCommands.canGetNext());
		back.setEnabled(sqlCommands.canGetPrevious());
		if (textInput.getText().trim().length() > 0)
			save.setEnabled(true);
		else
			save.setEnabled(false);
	}

	public void actionPerformed(ActionEvent ae) {
		try {
			String actionCommand = ae.getActionCommand();
			if (actionCommand.equals("Execute")) {
				execute();
			} else if (actionCommand.equals("Back")) {
				goBackInHistory();
			} else if (actionCommand.equals("Forward")) {
				goForwardInHistory();
			} else if (actionCommand.equals("Undo"))
				textInput.undo();
			else if (actionCommand.equals("Redo"))
				textInput.redo();
			else if (actionCommand.equals("Cut"))
				textInput.cut();
			else if (actionCommand.equals("Copy"))
				textInput.copy();
			else if (actionCommand.equals("Paste"))
				textInput.paste();
			else if (actionCommand.equals("Clear"))
				textInput.setText("");
			else if (actionCommand.equals("Select All"))
				textInput.selectAll();
			else if (actionCommand.equals("Open"))
				openFile();
			else if (actionCommand.equals("Save"))
				saveToFile();
			else if (actionCommand.equals("Empty")) {
				sqlCommands.clearHistory();
				reloadHistoryPanel();
				textInput.setText("");
				saveSQLCommandsToHistoryFile();
			} else if (actionCommand.equals("Delete history entry")) {
				deleteHistoryEntry();
			}
		} catch (VectorIndexOutOfBoundsException ve) {
			Toolkit.getDefaultToolkit().beep();
		} catch (CannotUndoException e) {
			Toolkit.getDefaultToolkit().beep();
		} catch (CannotRedoException e) {
			Toolkit.getDefaultToolkit().beep();
		} finally {
			updateToolBarButtons();
		}
	}

	private void execute() {
		sqlCommands.insertString(textInput.getText());
		reloadHistoryPanel();
		saveSQLCommandsToHistoryFile();
		sqlMinusObject.executeStatement(textInput.getText());
	}

	private void goBackInHistory() throws VectorIndexOutOfBoundsException {
		textInput.setText(sqlCommands.getPrevious(textInput.getText()));
		textInput.discardAllEdits();
		if (sqlCommands.getSelectedIndex() > 0) {
			historyList.setSelectedIndex(sqlCommands.getSelectedIndex() - 1);
		}
	}

	private void goForwardInHistory() throws VectorIndexOutOfBoundsException {
		textInput.setText(sqlCommands.getNext(textInput.getText()));
		textInput.discardAllEdits();
		if (sqlCommands.getSelectedIndex() > 0) {
			historyList.setSelectedIndex(sqlCommands.getSelectedIndex() - 1);
		}
	}

	private void deleteHistoryEntry() {
		if (sqlCommands.getSelectedIndex() > 0) {
			sqlCommands.deleteStringAt(sqlCommands.getSelectedIndex());
		}
		textInput.setText("");
		updateToolBarButtons();
		reloadHistoryPanel();
		saveSQLCommandsToHistoryFile();
	}

	public void changedUpdate(DocumentEvent de) {
		setLineNumbers();
		updateToolBarButtons();
	}

	public void insertUpdate(DocumentEvent de) {
		setLineNumbers();
		updateToolBarButtons();
	}

	public void removeUpdate(DocumentEvent de) {
		setLineNumbers();
		updateToolBarButtons();
	}

	public void closePopup() {
		popup.setVisible(false);
	}

	private void setLineNumbers() {
		SwingUtilities.invokeLater(lineNumberSetter);
	}

	public void focusGained(FocusEvent e) {
		textInput.requestFocusInWindow();
	}

	public void focusLost(FocusEvent e) {
		// nothing to do
	}

	private class LineNumberSetter implements java.lang.Runnable {
		public void run() {
			// System.err.println("Setting line numbers");
			if (lineNumberPanel.getLineCount() - 1 > textInput.getLineCount()) {
				// System.err.print("Removing lines");
				for (int i = lineNumberPanel.getLineCount() - 1; i > textInput.getLineCount(); i--) {
					try {
						lineNumberPanel.replaceRange("", lineNumberPanel.getLineStartOffset(i - 1),
								lineNumberPanel.getLineEndOffset(i - 1));
						// System.err.print(" "+i);
					} catch (javax.swing.text.BadLocationException e) {
						Toolkit.getDefaultToolkit().beep();
					}
				}
				// System.err.println();
			} else if (lineNumberPanel.getLineCount() - 1 < textInput.getLineCount()) {
				// System.err.print("Adding lines");
				for (int i = lineNumberPanel.getLineCount() - 1; i < textInput.getLineCount(); i++) {
					lineNumberPanel.append((i + 1) + lineSeparator);
					// System.err.print(" "+(i+1));
				}
				// System.err.println();
			}
		}
	}

	/* Implementation of the ChangeListener */
	public void stateChanged(ChangeEvent ae) {
		Point p = new Point();
		p.setLocation(0.00, textSPane.getViewport().getViewPosition().getY());
		lineSPane.getViewport().setViewPosition(p);
	}

	private void saveToFile() {
		int returnValue = fileChooser.showSaveDialog(this);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String s = fileChooser.getSelectedFile().getPath();
			File f = new File(s);
			if (f.exists()) {
				int overwrite = JOptionPane.showConfirmDialog(this, "Do you want to overwrite the file", "Overwrite?",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (overwrite == JOptionPane.YES_OPTION)
					SwingUtilities.invokeLater(fileSaver.setFile(f));
			} else {
				SwingUtilities.invokeLater(fileSaver.setFile(f));
			}
		}
	}

	private class FileSaverThread implements java.lang.Runnable {
		private File f;

		public FileSaverThread setFile(File f) {
			this.f = f;
			return this;
		}

		public void run() {
			if (f != null) {
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(f));
					writer.write(textInput.getText(), 0, textInput.getText().length());
					writer.flush();
					writer.close();
				} catch (IOException ie) {
					JOptionPane.showMessageDialog(null, ie);
					// Toolkit.getDefaultToolkit().beep();
				} finally {
					f = null;
				}
			}
		}
	}

	private class FileOpenerThread implements java.lang.Runnable {
		private File f;

		public FileOpenerThread setFile(File f) {
			this.f = f;
			return this;
		}

		public void run() {
			if (f != null) {
				try {
					textInput.setText("");
					// try to start reading
					BufferedReader reader = new BufferedReader(new FileReader(f));
					char[] buff = new char[4096];
					int nch;
					while ((nch = reader.read(buff, 0, buff.length)) != -1) {
						textInput.append(new String(buff, 0, nch));
					}
					reader.close();
				} catch (java.io.IOException ie) {
					JOptionPane.showMessageDialog(null, ie);
					// Toolkit.getDefaultToolkit().beep();
				} finally {
					f = null;
				}
			}
		}
	}

	private void openFile() {
		int returnValue = fileChooser.showOpenDialog(this);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String s = fileChooser.getSelectedFile().getPath();
			File f = new File(s);
			if (f.exists()) {
				SwingUtilities.invokeLater(fileOpener.setFile(f));
			}
		}
	}

	private void saveSQLCommandsToHistoryFile() {
		Thread saveThread = new Thread(() -> {
			try {
				List<String> sqlHistory = new ArrayList<>(sqlCommands);
				SQLHistoryHelper.saveSQLCommandsToHistory(sqlHistory, sqlMinusPreferences);
			} catch (Exception e) {
				sqlMinusObject.popMessage("Error saving SQL History. " + e.getMessage());
			}
		});
		SwingUtilities.invokeLater(saveThread);
	}

}
