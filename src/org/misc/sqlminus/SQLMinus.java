package org.misc.sqlminus;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.metal.MetalComboBoxEditor;
import javax.swing.text.JTextComponent;

import nocom.special.CustomizedMouseAdapter;
import nocom.special.ImageReader;
import nocom.special.LookAndFeelMenu;

public class SQLMinus extends JFrame implements ActionListener {

	private JTextArea textOutput;
	private SQLFrame textareaFrame;
	private JTextField textf1, textf2, textf3, tableText, columnText, schemaText, statusBar;
	private JComboBox sqlText;
	private JTextField minColWidth, maxColWidth, interColSpace, maxDataLength, nullRep;
	private JButton minColWidthButton, maxColWidthButton, interColSpaceButton, maxDataLengthButton;
	private JPasswordField passf;
	private JButton indicatorLabel;
	private JLabel schemaLabel;
	private JSplitPane splitPane;
	private Connection conn;
	private Statement stmt;
	private JCheckBox displayTextAreaButton, clearScreenButton, setCommitButton, rowDividers, enableThreads;
	private JComboBox rowsComboBox;
	private DisplayResultSet displayObject = new DisplayResultSet();
	private DisplayResultSetAsGrid displayGrid = new DisplayResultSetAsGrid();
	private ClassLoaderPanel driverPanel;
	private volatile boolean busy = false;
	public LookAndFeelMenu laf = new LookAndFeelMenu(new Component[] {}, KeyEvent.VK_L, null);
	public CustomizedMouseAdapter commonAdapter = new CustomizedMouseAdapter(false);
	private SortableTable tableOutput;
	private JRadioButton btText, btTable;
	private JScrollPane textSpane, tableSpane;
	private final int MINCOLWIDTH = 15, MAXCOLWIDTH = 50, INTERCOLSPACE = 4, MAXDATALENGTH = 1000;
	private final String COMMIT_TRANSACTIONS_COMMAND = "COMMIT_TRANSACTIONS";
	private final String ROLLBACK_TRANSACTIONS_COMMAND = "ROLLBACK_TRANSACTIONS";

	/************** The Constructor for SQLMinus ***********************/

	public SQLMinus() {
		super("SQL Minus");

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		// get the font attributes for the normal font
		String fName = ResourceLoader.getResourceString("fName");
		int fStyle = ResourceLoader.getFontStyle("fStyle");
		int fSize = ResourceLoader.getFontSize("fSize");
		Font f = new Font(fName, fStyle, fSize);

		// get the font attributes for the text field font
		String tfontName = ResourceLoader.getResourceString("tfontName");
		int tfontStyle = ResourceLoader.getFontStyle("tfontStyle");
		int tfontSize = ResourceLoader.getFontSize("tfontSize");
		Font tfont = new Font(tfontName, tfontStyle, tfontSize);

		// Font f=new Font("Impact",Font.PLAIN,14);
		// Font tfont=new Font("Courier New",Font.BOLD,15);
		// Color backgroundColor=new Color(209,210,231);
		Color backgroundColor = null;/////////////////////////////////////////////////////////////////////////
		// Color backgroundLight=new Color(191,192,255);
		Color backgroundLight = null;//////////////////////////////////////////////////////////////////////////
		Color buttonColor = new Color(161, 161, 255);
		Image iconImage = new ImageIcon().getImage();
		try {
			iconImage = ImageReader.getImage(this.getClass(), "/images/sqlminus.gif");
		} catch (Exception e) {
		}
		// try{iconImage=new
		// ImageReader().getImage("SQLMinus.jar","/images/icon.png");}catch(Exception
		// e){}
		laf.addComponentToMonitor(commonAdapter.getPopupMenu());

		/************ The Connection panel components ************/

		JPanel connectionPanel = new JPanel(gridbag);

		c.insets = new Insets(8, 8, 8, 8);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 1;
		c.weightx = 1;
		JLabel label1 = new JLabel("Driver Name", JLabel.RIGHT);
		// label1.setFont(f);
		gridbag.setConstraints(label1, c);
		connectionPanel.add(label1);

		c.weightx = 4;
		c.gridwidth = GridBagConstraints.REMAINDER;
		textf1 = new JTextField("sun.jdbc.odbc.JdbcOdbcDriver", 50);
		textf1.setActionCommand("CONNECT");
		textf1.addActionListener(this);
		textf1.addMouseListener(commonAdapter);
		// textf1.setFont(tfont);
		gridbag.setConstraints(textf1, c);
		connectionPanel.add(textf1);

		c.weightx = 1;
		c.gridwidth = 1;
		JLabel label2 = new JLabel("Connect String", JLabel.RIGHT);
		// label2.setFont(f);
		gridbag.setConstraints(label2, c);
		connectionPanel.add(label2);

		c.weightx = 4;
		c.gridwidth = GridBagConstraints.REMAINDER;
		textf2 = new JTextField("jdbc:odbc:test", 50);
		textf2.setActionCommand("CONNECT");
		textf2.addActionListener(this);
		textf2.addMouseListener(commonAdapter);
		// textf2.setFont(tfont);
		gridbag.setConstraints(textf2, c);
		connectionPanel.add(textf2);

		c.weightx = 1;
		c.gridwidth = 1;
		JLabel label3 = new JLabel("Username", JLabel.RIGHT);
		// label3.setFont(f);
		gridbag.setConstraints(label3, c);
		connectionPanel.add(label3);

		textf3 = new JTextField(20);
		textf3.setActionCommand("CONNECT");
		textf3.addActionListener(this);
		textf3.addMouseListener(commonAdapter);
		// textf3.setFont(tfont);
		gridbag.setConstraints(textf3, c);
		connectionPanel.add(textf3);

		JLabel label4 = new JLabel("Password", JLabel.RIGHT);
		// label4.setFont(f);
		gridbag.setConstraints(label4, c);
		connectionPanel.add(label4);

		c.gridwidth = GridBagConstraints.REMAINDER;
		passf = new JPasswordField(20);
		passf.setActionCommand("CONNECT");
		passf.addActionListener(this);
		// passf.setFont(tfont);
		passf.setEchoChar('*');
		gridbag.setConstraints(passf, c);
		connectionPanel.add(passf);

		c.gridwidth = 1;
		JButton button2 = new JButton("CLEAR FIELDS");
		button2.addActionListener(this);
		// button2.setFont(f);
		button2.setBackground(buttonColor);
		button2.setForeground(Color.white);
		gridbag.setConstraints(button2, c);
		connectionPanel.add(button2);

		JButton button3 = new JButton("CONNECT");
		button3.addActionListener(this);
		// button3.setFont(f);
		button3.setBackground(buttonColor);
		button3.setForeground(Color.white);
		gridbag.setConstraints(button3, c);
		connectionPanel.add(button3);

		JButton disconnectButton = new JButton("DISCONNECT");
		disconnectButton.addActionListener(this);
		// disconnectButton.setFont(f);
		disconnectButton.setBackground(buttonColor);
		disconnectButton.setForeground(Color.white);
		gridbag.setConstraints(disconnectButton, c);
		connectionPanel.add(disconnectButton);

		JButton button4 = new JButton("Show Sample 1");
		button4.addActionListener(this);
		// button4.setFont(f);
		// button4.setToolTipText("Shows the usual configuration for connecting with Sun
		// jdbc odbc bridge driver");
		button4.setToolTipText(ResourceLoader.getResourceString("sample1ToolTip"));
		button4.setBackground(buttonColor);
		button4.setForeground(Color.white);
		gridbag.setConstraints(button4, c);
		connectionPanel.add(button4);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = GridBagConstraints.REMAINDER;
		JButton button5 = new JButton("Show Sample 2");
		button5.addActionListener(this);
		// button5.setFont(f);
		// button5.setToolTipText("Shows the usual configuration for connecting with
		// Oracle thin driver");
		button5.setToolTipText(ResourceLoader.getResourceString("sample2ToolTip"));
		button5.setBackground(buttonColor);
		button5.setForeground(Color.white);
		gridbag.setConstraints(button5, c);
		connectionPanel.add(button5);

		connectionPanel.setBackground(backgroundColor);

		/*****************
		 * Connection Panel components end here******** now the optionsPanel is added
		 ***************************************************************/

		JPanel optionsPanel = new JPanel(gridbag);

		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 10;
		c.weighty = 1;
		/*
		 * JLabel dummy1=new JLabel(""); dummy1.setFont(f);
		 * gridbag.setConstraints(dummy1,c); optionsPanel.add(dummy1);
		 */

		displayTextAreaButton = new JCheckBox("Show query window", false);
		// displayTextAreaButton.setFont(f);
		displayTextAreaButton.setBackground(backgroundColor);
		displayTextAreaButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				if (ie.getStateChange() == ItemEvent.SELECTED) {
					openSqlFrame();
				} else {
					closeSqlFrame();
				}
			}
		});
		displayTextAreaButton.setToolTipText("Use a separate window to enter and execute queries");
		// gridbag.setConstraints(displayTextAreaButton,c);
		// optionsPanel.add(displayTextAreaButton);
		JPanel displayTextAreaPanel = new JPanel();
		displayTextAreaPanel.add(displayTextAreaButton);
		displayTextAreaPanel.setBackground(backgroundColor);
		displayTextAreaPanel.setBorder(BorderFactory.createEtchedBorder());
		gridbag.setConstraints(displayTextAreaPanel, c);
		optionsPanel.add(displayTextAreaPanel);

		clearScreenButton = new JCheckBox("Clear screen before each result", true);
		// clearScreenButton.setFont(f);
		clearScreenButton.setBackground(backgroundColor);
		// gridbag.setConstraints(clearScreenButton,c);
		// optionsPanel.add(clearScreenButton);
		JPanel clearScreenPanel = new JPanel();
		clearScreenPanel.add(clearScreenButton);
		clearScreenPanel.setBackground(backgroundColor);
		clearScreenPanel.setBorder(BorderFactory.createEtchedBorder());
		gridbag.setConstraints(clearScreenPanel, c);
		optionsPanel.add(clearScreenPanel);

		// -------------------------------------------------------------------------------------
		JLabel rowsLabel = new JLabel("Fetch records", JLabel.RIGHT);
		// rowsLabel.setToolTipText("Enter the number of records to be returned on
		// executing a query");
		// rowsLabel.setFont(f);
		// gridbag.setConstraints(rowsLabel,c);
		// optionsPanel.add(rowsLabel);

		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		String[] options = { "100", "500", "All" };
		rowsComboBox = new JComboBox(options);
		rowsComboBox.setEditable(true);
		// rowsComboBox.setFont(tfont);
		rowsComboBox.setBackground(backgroundColor);
		// rowsComboBox.setToolTipText("Enter the number of records to be returned on
		// executing a query");
		// gridbag.setConstraints(rowsComboBox,c);
		// optionsPanel.add(rowsComboBox);

		JPanel rowsPanel = new JPanel();
		rowsPanel.setBackground(backgroundColor);
		rowsPanel.setBorder(BorderFactory.createEtchedBorder());
		rowsPanel.add(rowsLabel);
		rowsPanel.add(rowsComboBox);
		rowsPanel.setToolTipText("Enter the number of records to be returned on executing a query");
		gridbag.setConstraints(rowsPanel, c);
		optionsPanel.add(rowsPanel);
		// --------------------------------------------------------------------------------------

		c.gridwidth = 1;
		c.weightx = 10;
		/*
		 * JLabel dummy2=new JLabel(""); dummy2.setFont(f);
		 * gridbag.setConstraints(dummy2,c); optionsPanel.add(dummy2);
		 */

		setCommitButton = new JCheckBox("Auto Commit", false);
		// setCommitButton.setFont(f);
		setCommitButton.setBackground(backgroundColor);
		setCommitButton.setToolTipText(
				"If selected then all SQL statements are committed immediately after the statement's completion");
		setCommitButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				try {
					setCommit();
				} catch (SQLException e) {
					popMessage(e.getMessage());
				}
			}
		});
		// gridbag.setConstraints(setCommitButton,c);
		// optionsPanel.add(setCommitButton);
		JPanel setCommitPanel = new JPanel();

		JPanel setCommitInnerPanel = new JPanel();
		setCommitInnerPanel.setLayout(new BoxLayout(setCommitInnerPanel, BoxLayout.X_AXIS));
		setCommitInnerPanel.add(setCommitButton);

		JButton rollBackButton;
		try {
			rollBackButton = new JButton(new ImageIcon(ImageReader.getImage(this.getClass(), "/images/rollback.gif")));
		} catch (Exception e) {
			rollBackButton = new JButton("Rollback");
		}
		rollBackButton.setActionCommand(ROLLBACK_TRANSACTIONS_COMMAND);
		rollBackButton.addActionListener(this);
		rollBackButton.setToolTipText("Rollback");
		rollBackButton.setPreferredSize(new Dimension(30, 30));
		setCommitInnerPanel.add(rollBackButton);

		JButton commitButton;
		try {
			commitButton = new JButton(new ImageIcon(ImageReader.getImage(this.getClass(), "/images/commit.gif")));
		} catch (Exception e) {
			commitButton = new JButton("Commit");
		}
		commitButton.setActionCommand(COMMIT_TRANSACTIONS_COMMAND);
		commitButton.addActionListener(this);
		commitButton.setToolTipText("Commit");
		commitButton.setPreferredSize(new Dimension(30, 30));
		setCommitInnerPanel.add(commitButton);

		setCommitInnerPanel.setBackground(backgroundColor);

		setCommitPanel.add(setCommitInnerPanel);
		setCommitPanel.setBackground(backgroundColor);
		setCommitPanel.setBorder(BorderFactory.createEtchedBorder());

		gridbag.setConstraints(setCommitPanel, c);
		optionsPanel.add(setCommitPanel);

		enableThreads = new JCheckBox("Enable threads", false);
		// enableThreads.setFont(f);
		enableThreads.setBackground(backgroundColor);
		enableThreads.setToolTipText("Use a separate thread for displaying the result");
		JPanel enableThreadsPanel = new JPanel();
		enableThreadsPanel.add(enableThreads);
		enableThreadsPanel.setBackground(backgroundColor);
		enableThreadsPanel.setBorder(BorderFactory.createEtchedBorder());
		gridbag.setConstraints(enableThreadsPanel, c);
		optionsPanel.add(enableThreadsPanel);

		btTable = new JRadioButton("Output in grid");
		btTable.setSelected(true);
		// btTable.setFont(f);
		btTable.addActionListener(this);
		btText = new JRadioButton("Output as text");
		btText.setSelected(false);
		// btText.setFont(f);
		btText.addActionListener(this);
		ButtonGroup btGroup = new ButtonGroup();
		btGroup.add(btTable);
		btGroup.add(btText);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(btTable);
		buttonPanel.add(btText);
		buttonPanel.setBackground(backgroundColor);
		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
		gridbag.setConstraints(buttonPanel, c);
		optionsPanel.add(buttonPanel);

		optionsPanel.setBackground(backgroundColor);

		/********************
		 * optionsPanel components end here******************* now the
		 * displayFormatPanel components are created
		 ***********/

		JPanel displayFormatPanel = new JPanel(gridbag);

		c.weightx = 1;
		c.weighty = 1;
		c.gridheight = 1;
		c.gridwidth = 1;

		// -----------------------------------------------------------------------------------------
		maxColWidth = new JTextField("" + MAXCOLWIDTH);
		maxColWidth.setToolTipText("Value must be greater than 0");
		// maxColWidth.setFont(tfont);
		maxColWidth.addMouseListener(commonAdapter);
		maxColWidthButton = new JButton("Maximum column width :");
		maxColWidthButton.setToolTipText("Click to reset to default value");
		maxColWidthButton.addActionListener(this);
		// maxColWidthButton.setFont(f);

		minColWidth = new JTextField("" + MINCOLWIDTH);
		minColWidth.setToolTipText("Value must be 0 or greater");
		// minColWidth.setFont(tfont);
		minColWidth.addMouseListener(commonAdapter);
		minColWidthButton = new JButton("Minimum column width :");
		minColWidthButton.setToolTipText("Click to reset to default value");
		minColWidthButton.addActionListener(this);
		// minColWidthButton.setFont(f);

		interColSpace = new JTextField("" + INTERCOLSPACE);
		interColSpace.setToolTipText("Value must be 0 or greater");
		// interColSpace.setFont(tfont);
		interColSpace.addMouseListener(commonAdapter);
		interColSpaceButton = new JButton("Inter-column spacing :");
		interColSpaceButton.setToolTipText("Click to reset to default value");
		interColSpaceButton.addActionListener(this);
		// interColSpaceButton.setFont(f);

		maxDataLength = new JTextField("" + MAXDATALENGTH);
		maxDataLength.setToolTipText("Value must be greater than 0");
		// maxDataLength.setFont(tfont);
		maxDataLength.addMouseListener(commonAdapter);
		maxDataLengthButton = new JButton("Maximum data length :");
		maxDataLengthButton.setToolTipText("Click to reset to default value");
		maxDataLengthButton.addActionListener(this);
		// maxDataLengthButton.setFont(f);

		JPanel combinedPanel = new JPanel(new GridLayout(4, 2, 3, 3));
		combinedPanel.add(minColWidthButton);
		combinedPanel.add(minColWidth);
		combinedPanel.add(maxColWidthButton);
		combinedPanel.add(maxColWidth);
		combinedPanel.add(interColSpaceButton);
		combinedPanel.add(interColSpace);
		combinedPanel.add(maxDataLengthButton);
		combinedPanel.add(maxDataLength);
		combinedPanel.setBorder(BorderFactory.createEtchedBorder());
		gridbag.setConstraints(combinedPanel, c);
		displayFormatPanel.add(combinedPanel);
		// ---------------------------------------------------------------------------------------------------

		// ---------------------------------------------------------------------------------------------------
		nullRep = new JTextField("<NULL>");
		nullRep.setToolTipText("Enter the string to display for null values");
		nullRep.addMouseListener(commonAdapter);
		// nullRep.setFont(tfont);
		JLabel nullRepLabel = new JLabel("Null representation :", JLabel.RIGHT);
		// nullRepLabel.setFont(f);
		JPanel nullRepPanel = new JPanel();
		nullRepPanel.setLayout(new BoxLayout(nullRepPanel, BoxLayout.X_AXIS));
		nullRepPanel.add(nullRepLabel);
		nullRepPanel.add(nullRep);
		nullRepPanel.setBorder(BorderFactory.createEtchedBorder());

		rowDividers = new JCheckBox("Always show row dividers", false);
		rowDividers.setToolTipText("Show row dividers even for non-multiline result sets");
		// rowDividers.setFont(f);
		rowDividers.setBackground(backgroundColor);
		JPanel rowDividersPanel = new JPanel();
		rowDividersPanel.add(rowDividers);
		rowDividersPanel.setBackground(backgroundColor);
		rowDividersPanel.setBorder(BorderFactory.createEtchedBorder());

		c.gridwidth = GridBagConstraints.REMAINDER;
		JPanel combinedPanel2 = new JPanel(new GridLayout(2, 1, 16, 16));
		combinedPanel2.add(nullRepPanel);
		combinedPanel2.add(rowDividersPanel);
		gridbag.setConstraints(combinedPanel2, c);
		displayFormatPanel.add(combinedPanel2);
		// ----------------------------------------------------------------------------------------------------

		displayFormatPanel.setBackground(backgroundColor);

		/***************
		 * displayFormatPanel components end here***************** now the miscPanel
		 * components are created
		 **********/

		JPanel miscPanel = new JPanel(gridbag);

		c.weightx = 1;
		c.weighty = 1;
		c.gridheight = 1;
		c.gridwidth = 1;
		JLabel tableName = new JLabel("Table Pattern", JLabel.RIGHT);
		// tableName.setFont(f);
		gridbag.setConstraints(tableName, c);
		miscPanel.add(tableName);

		c.weightx = 100;
		tableText = new JTextField("%", 20);
		// tableText.setFont(tfont);
		tableText.setActionCommand("GET TABLES");
		tableText.addActionListener(this);
		tableText.addMouseListener(commonAdapter);
		gridbag.setConstraints(tableText, c);
		miscPanel.add(tableText);

		c.weightx = 1;
		JLabel columnName = new JLabel("Column Pattern", JLabel.RIGHT);
		// columnName.setFont(f);
		gridbag.setConstraints(columnName, c);
		miscPanel.add(columnName);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 100;
		columnText = new JTextField("%", 20);
		// columnText.setFont(tfont);
		columnText.setActionCommand("GET COLUMNS");
		columnText.addActionListener(this);
		columnText.addMouseListener(commonAdapter);
		gridbag.setConstraints(columnText, c);
		miscPanel.add(columnText);

		c.weightx = 1;
		c.gridwidth = 1;
		schemaLabel = new JLabel("Schema Pattern", JLabel.RIGHT);
		schemaLabel.setEnabled(false);
		// schemaLabel.setFont(f);
		gridbag.setConstraints(schemaLabel, c);
		miscPanel.add(schemaLabel);

		/*
		 * JLabel dummy4=new JLabel(""); dummy4.setFont(f);
		 * gridbag.setConstraints(dummy4,c); miscPanel.add(dummy4);
		 */
		// --------------------------------------------------------------------------------------------------------
		// c.weightx=2;
		schemaText = new JTextField("%", 20);
		schemaText.setEnabled(false);
		// schemaText.setFont(tfont);
		schemaText.addMouseListener(commonAdapter);
		JCheckBox schemaBox = new JCheckBox("Enable", false);
		// schemaBox.setFont(f);
		schemaBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				boolean isEnabled = ((JCheckBox) ae.getSource()).isSelected();
				schemaLabel.setEnabled(isEnabled);
				schemaText.setEnabled(isEnabled);
			}
		});
		JPanel schemaPanel = new JPanel();
		schemaPanel.setLayout(new BoxLayout(schemaPanel, BoxLayout.X_AXIS));
		schemaPanel.setBorder(BorderFactory.createEtchedBorder());
		schemaPanel.add(schemaBox);
		schemaPanel.add(schemaText);
		gridbag.setConstraints(schemaPanel, c);
		miscPanel.add(schemaPanel);
		// ------------------------------------------------------------------------------------------------------

		JLabel dummy5 = new JLabel("");
		// dummy5.setFont(f);
		gridbag.setConstraints(dummy5, c);
		miscPanel.add(dummy5);

		JButton getTableButton = new JButton("GET TABLES");
		getTableButton.addActionListener(this);
		// getTableButton.setFont(f);
		getTableButton.setToolTipText("Displays the tables matching the table pattern and their descriptions");
		getTableButton.setBackground(buttonColor);
		getTableButton.setForeground(Color.white);
		gridbag.setConstraints(getTableButton, c);
		miscPanel.add(getTableButton);

		c.gridwidth = GridBagConstraints.REMAINDER;
		JButton getColumnButton = new JButton("GET COLUMNS");
		getColumnButton.addActionListener(this);
		// getColumnButton.setFont(f);
		getColumnButton.setToolTipText("Displays the matching columns in the matching tables and their descriptions");
		getColumnButton.setBackground(buttonColor);
		getColumnButton.setForeground(Color.white);
		gridbag.setConstraints(getColumnButton, c);
		miscPanel.add(getColumnButton);

		c.gridwidth = 1;
		c.weighty = 0;
		JLabel dummy6 = new JLabel("");
		// dummy6.setFont(f);
		gridbag.setConstraints(dummy6, c);
		miscPanel.add(dummy6);

		JLabel dummy7 = new JLabel("");
		// dummy7.setFont(f);
		gridbag.setConstraints(dummy7, c);
		miscPanel.add(dummy7);

		JLabel dummy8 = new JLabel("");
		// dummy8.setFont(f);
		gridbag.setConstraints(dummy8, c);
		miscPanel.add(dummy8);

		JButton getSchemaButton = new JButton("GET SCHEMAS");
		getSchemaButton.addActionListener(this);
		// getSchemaButton.setFont(f);
		getSchemaButton.setToolTipText("Displays all the schemas");
		getSchemaButton.setBackground(buttonColor);
		getSchemaButton.setForeground(Color.white);
		gridbag.setConstraints(getSchemaButton, c);
		miscPanel.add(getSchemaButton);

		c.gridwidth = GridBagConstraints.REMAINDER;
		JButton getCatalogButton = new JButton("GET CATALOGS");
		getCatalogButton.addActionListener(this);
		// getCatalogButton.setFont(f);
		getCatalogButton.setToolTipText("Displays all the catalogs");
		getCatalogButton.setBackground(buttonColor);
		getCatalogButton.setForeground(Color.white);
		gridbag.setConstraints(getCatalogButton, c);
		miscPanel.add(getCatalogButton);

		miscPanel.setBackground(backgroundColor);

		/******************
		 * miscPanel components end here***************** now its the turn of the
		 * driverPanel
		 ***********/

		driverPanel = new ClassLoaderPanel(laf, c.insets, backgroundColor, buttonColor, f, tfont);

		/****************
		 * driverPanel components end here******************** now the Tabbed Pane is
		 * created
		 ************************/

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
		tabbedPane.addTab("Connection", connectionPanel);
		tabbedPane.addTab("Options", optionsPanel);
		tabbedPane.addTab("Display", displayFormatPanel);
		tabbedPane.addTab("Misc", miscPanel);
		tabbedPane.addTab("Drivers", driverPanel);
		tabbedPane.setMinimumSize(new Dimension(0, 0));
		// tabbedPane.setFont(f);
		tabbedPane.setBackground(backgroundColor);

		/********************
		 * Tabbed pane components end here************ now Bottom Panel components are
		 * defined and added
		 ************************************************************/
		JPanel bottomPanel = new JPanel(gridbag);

		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 50;
		c.weighty = 1;
		// sqlText=new JTextField("select * from emp",50);
		Object[] optionArray = { makeObj(ResourceLoader.getResourceString("sample1SQLStatement")),
				makeObj(ResourceLoader.getResourceString("sample2SQLStatement")) };
		sqlText = new JComboBox(optionArray);
		MetalComboBoxEditor editor = new MetalComboBoxEditor();
		sqlText.setEditor(editor);
		sqlText.setEditable(true);
		sqlText.setToolTipText("Enter SQL statement and press Enter to execute");
		sqlText.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {
				if ((ke.getKeyCode() == KeyEvent.VK_ENTER)
						|| ((ke.getKeyCode() == KeyEvent.VK_E) && ((ke.getModifiers() & InputEvent.CTRL_MASK) != 0))) {
					insertItem(sqlText, getSqlText());
					executeStatement(getSqlText());
				} else if ((ke.getKeyCode() == KeyEvent.VK_W) && ((ke.getModifiers() & InputEvent.CTRL_MASK) != 0)) {
					showSQLFrame(true);
				}
			}
		});

		// sqlText.getEditor().getEditorComponent().addMouseListener(commonAdapter);
		CustomizedMouseAdapter sqlAdapter = new CustomizedMouseAdapter(false);

		JMenuItem execItem;// =new JMenuItem("Execute");
		try {
			execItem = new JMenuItem("Execute",
					new ImageIcon(ImageReader.getImage(this.getClass(), "/images/execute.gif")));
		} catch (Exception e) {
			execItem = new JMenuItem("Execute");
		}
		execItem.setActionCommand("EXECUTE SQLTEXT");
		execItem.addActionListener(this);
		/*
		 * execItem.addActionListener(new ActionListener(){ public void
		 * actionPerformed(ActionEvent ae){ insertItem(sqlText,getSqlText());
		 * executeStatement(getSqlText()); } });
		 */

		JMenuItem emptyCombo = new JMenuItem("Empty contents");
		emptyCombo.setActionCommand("EMPTY COMBOBOX");
		emptyCombo.addActionListener(this);

		JMenuItem showQueryWindow = new JMenuItem("Show query window");
		showQueryWindow.setActionCommand("SHOW QUERY WINDOW-COMBO COMMAND");
		showQueryWindow.addActionListener(this);

		sqlAdapter.getPopupMenu().addSeparator();
		sqlAdapter.getPopupMenu().add(emptyCombo);
		sqlAdapter.getPopupMenu().add(showQueryWindow);
		sqlAdapter.getPopupMenu().addSeparator();
		sqlAdapter.getPopupMenu().add(execItem);

		sqlText.getEditor().getEditorComponent().addMouseListener(sqlAdapter);
		laf.addComponentToMonitor(sqlAdapter.getPopupMenu());
		sqlText.setFont(tfont);
		gridbag.setConstraints(sqlText, c);
		bottomPanel.add(sqlText);

		c.weightx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		// indicatorLabel=new JLabel("Not connected",JLabel.LEFT);
		indicatorLabel = new JButton("Not connected");
		indicatorLabel.setBorder(BorderFactory.createEmptyBorder());
		indicatorLabel.setActionCommand("KILL THREAD");
		indicatorLabel.addActionListener(this);
		// indicatorLabel.setFont(f);
		indicatorLabel.setForeground(Color.red);
		gridbag.setConstraints(indicatorLabel, c);
		bottomPanel.add(indicatorLabel);

		textOutput = new JTextArea();
		textOutput.setFont(tfont);
		// textOutput.setSelectionColor(Color.white);
		// textOutput.setSelectionColor(new Color(180,180,180));
		textOutput.setEditable(false);
		textOutput.setBackground(backgroundLight);
		CustomizedMouseAdapter textOutputAdapter = new CustomizedMouseAdapter(true);
		laf.addComponentToMonitor(textOutputAdapter.getPopupMenu());
		textOutput.addMouseListener(textOutputAdapter);

		textSpane = new JScrollPane(textOutput);

		c.weighty = 2000;
		c.weightx = 2000;
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = GridBagConstraints.RELATIVE;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(textSpane, c);
		bottomPanel.add(textSpane);

		// adding a jtable to the bottompanel(to be used instead of the text area if so
		// desired
		tableOutput = new SortableTable(new DisplayResultSetTableModel(), MINCOLWIDTH, MAXCOLWIDTH, backgroundLight,
				iconImage, tfont);
		tableOutput.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableOutput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableOutput.setBackground(backgroundColor);
		tableOutput.setFont(tfont);
		tableOutput.setCellEditorFont(tfont);
		laf.addComponentToMonitor(tableOutput.getCellEditorPopupMenu());
		laf.addComponentToMonitor(tableOutput.getContentsViewFrame());
		laf.addComponentToMonitor(tableOutput.getContentsViewPopupMenu());
		// tableOutput.setBorder(BorderFactory.createEtchedBorder());

		tableSpane = new JScrollPane(tableOutput);

		c.fill = GridBagConstraints.BOTH;
		c.gridheight = GridBagConstraints.RELATIVE;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(tableSpane, c);
		bottomPanel.add(tableSpane);
		tableSpane.setVisible(false);

		statusBar = new JTextField();
		statusBar.setEditable(false);
		statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusBar.setBackground(null);
		c.weightx = 1;
		c.weighty = 1;
		c.insets = new Insets(0, 0, 0, 0);
		gridbag.setConstraints(statusBar, c);
		bottomPanel.add(statusBar);

		c.insets = new Insets(8, 8, 8, 8);
		bottomPanel.setBackground(backgroundColor);

		/**************
		 * Bottom Panel components end here************** now adding the tabbed pane and
		 * the bottom panel to a split pane and adding the split pane to the base frame
		 ************************************************************/

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, tabbedPane, bottomPanel);
		splitPane.setDividerSize(3);
		splitPane.setDividerLocation(162);
		splitPane.setBackground(backgroundColor);
		getContentPane().add(splitPane);

		/**************
		 * Now creating the initially invisible frame for entering SQL statements,the
		 * text area inside, the popup menu for the said text area and such.
		 ************************************************************/

		textareaFrame = new SQLFrame(this, tfont, f, backgroundLight, 0, 0);
		textareaFrame.getContentPane().setBackground(backgroundColor);
		textareaFrame.setIconImage(iconImage);
		textareaFrame.setBounds(150, 100, 600, 300);
		textareaFrame.setVisible(false);
		laf.addComponentToMonitor(textareaFrame);

		/*********
		 * Initially invisible frame components end here******* now a menubar is added
		 * to the parent frame as also a window listener
		 ************************************************************/

		JMenuItem item1 = new JMenuItem("Exit");
		item1.setBackground(backgroundColor);
		item1.addActionListener(this);
		item1.setMnemonic('x');

		JMenu fileMenu = new JMenu("File");
		fileMenu.setBackground(backgroundColor);
		fileMenu.setMnemonic('f');
		fileMenu.add(item1);

		final JCheckBoxMenuItem item2 = new JCheckBoxMenuItem("Settings", true);
		item2.setToolTipText("Hide the settings panel");
		item2.setBackground(backgroundColor);
		item2.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ie) {
				if (ie.getStateChange() == ItemEvent.SELECTED) {
					splitPane.setDividerLocation(162);
					item2.setToolTipText("Hide the settings panel");
				} else {
					splitPane.setDividerLocation(0);
					item2.setToolTipText("Show the settings panel");
				}
			}
		});
		item2.setMnemonic('t');

		JMenu viewMenu = new JMenu("View");
		viewMenu.setBackground(backgroundColor);
		viewMenu.setMnemonic('v');
		viewMenu.add(item2);

		// LookAndFeelMenu laf=new LookAndFeelMenu(this,KeyEvent.VK_L,backgroundColor);
		/*
		 * Component[] rootComponents={this,textareaFrame}; LookAndFeelMenu laf=new
		 * LookAndFeelMenu(rootComponents,KeyEvent.VK_L,backgroundColor);
		 */
		laf.addComponentToMonitor(this);
		laf.setBackground(backgroundColor);

		JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(backgroundColor);
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		menuBar.add(laf);

		setIconImage(iconImage);
		setJMenuBar(menuBar);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				exitSystem();
			}
		});
		pack();
		setBounds(10, 10, 950, 700);
		setVisible(true);
		enableTextOutputSettings(btText.isSelected());

	}

	/*********** The constructor for SQLMinus ends here ********************/

	public void actionPerformed(ActionEvent ae) {
		try {
			String command = ae.getActionCommand();
			if (command.equals("Exit"))
				exitSystem();
			else if (command.equals("GET COLUMNS"))
				getColumns();
			else if (command.equals("GET TABLES"))
				getTables();
			else if (command.equals("GET SCHEMAS"))
				getSchemas();
			else if (command.equals("GET CATALOGS"))
				getCatalogs();
			else if (command.equals("CLEAR FIELDS"))
				clearFields();
			else if (command.equals("CONNECT"))
				connectDatabase();
			else if (command.equals("DISCONNECT"))
				disconnectDatabase();
			else if (command.equals(ROLLBACK_TRANSACTIONS_COMMAND))
				rollbackTransactions();
			else if (command.equals(COMMIT_TRANSACTIONS_COMMAND))
				commitTransactions();
			else if (command.equals("Show Sample 1"))
				showSample1();
			else if (command.equals("Show Sample 2"))
				showSample2();
			else if (command.equals("Minimum column width :"))
				minColWidth.setText("" + MINCOLWIDTH);
			else if (command.equals("Maximum column width :"))
				maxColWidth.setText("" + MAXCOLWIDTH);
			else if (command.equals("Inter-column spacing :"))
				interColSpace.setText("" + INTERCOLSPACE);
			else if (command.equals("Maximum data length :"))
				maxDataLength.setText("" + MAXDATALENGTH);
			else if (command.equals("Output in grid"))
				enableTextOutputSettings(false);
			else if (command.equals("Output as text"))
				enableTextOutputSettings(true);
			else if (command.equals("KILL THREAD")) {
				displayObject.killThread();
				displayGrid.killThread();
			} else if (command.equals("EXECUTE SQLTEXT")) {
				insertItem(sqlText, getSqlText());
				executeStatement(getSqlText());
			} else if (command.equals("EMPTY COMBOBOX")) {
				sqlText.removeAllItems();
			} else if (command.equals("SHOW QUERY WINDOW-COMBO COMMAND")) {
				showSQLFrame(true);
			}
		} catch (NullPointerException ne) {
			popMessage("Possibly not connected");
		} catch (SQLException se) {
			popMessage(se.getMessage());
		} catch (Exception e) {
			popMessage(e.toString());
		}
	}

	public /* synchronized */ void exitSystem() {
		// Do not automatically commit on exit
//		try {
//			conn.commit();
//		} catch (Exception e) {
//		}
		try {
			conn.close();
		} catch (Exception e) {
		}
		System.exit(0);
	}

	public /* synchronized */ void clearTextOutput() {
		if (clearScreenButton.isSelected()) {
			textOutput.setText("");
		}
	}

	public /* synchronized */ void displayResultSet(ResultSet rst) {
		clearTextOutput();

		Integer rowsToReturn = null;
		// rowsToReturn will be null if all rows are to be returned
		if (!(((String) rowsComboBox.getSelectedItem()).equalsIgnoreCase("All"))) {
			try {
				rowsToReturn = Integer.valueOf(((String) rowsComboBox.getSelectedItem()).trim());
			} catch (NumberFormatException ne) {
				popMessageAndCloseResultSet("Enter an integer for the number of records to be returned", rst);
				rowsComboBox.requestFocusInWindow();
				return;
			}
		}

		int minColWidthValue = MINCOLWIDTH;
		try {
			if (minColWidth.isEnabled()) {
				minColWidthValue = Integer.parseInt(minColWidth.getText().trim());
				if (minColWidthValue < 0) {
					popMessageAndCloseResultSet("Maximum column width should be 0 or greater", rst);
					minColWidth.requestFocusInWindow();
					return;
				}
			}
		} catch (NumberFormatException ne) {
			popMessageAndCloseResultSet("Enter an integer value for Minimum column width", rst);
			minColWidth.requestFocusInWindow();
			return;
		}

		int maxColWidthValue = MAXCOLWIDTH;
		try {
			if (maxColWidth.isEnabled()) {
				maxColWidthValue = Integer.parseInt(maxColWidth.getText().trim());
				if (maxColWidthValue < 1) {
					popMessageAndCloseResultSet("Maximum column width should be greater than 0", rst);
					maxColWidth.requestFocusInWindow();
					return;
				}
			}
		} catch (NumberFormatException ne) {
			popMessageAndCloseResultSet("Enter an integer value for Maximum column width", rst);
			maxColWidth.requestFocusInWindow();
			return;
		}

		int interColSpaceValue = INTERCOLSPACE;
		try {
			if (interColSpace.isEnabled()) {
				interColSpaceValue = Integer.parseInt(interColSpace.getText().trim());
				if (interColSpaceValue < 0) {
					popMessageAndCloseResultSet("Inter-column spacing should be 0 or greater", rst);
					interColSpace.requestFocusInWindow();
					return;
				}
			}
		} catch (NumberFormatException ne) {
			popMessageAndCloseResultSet("Enter an integer value for Inter-column spacing", rst);
			interColSpace.requestFocusInWindow();
			return;
		}

		int maxDataLengthValue = MAXDATALENGTH;
		try {
			if (maxDataLength.isEnabled()) {
				maxDataLengthValue = Integer.parseInt(maxDataLength.getText().trim());
				if (maxDataLengthValue < 1) {
					popMessageAndCloseResultSet("Maximum data length should be greater than 0", rst);
					maxDataLength.requestFocusInWindow();
					return;
				}
			}
		} catch (NumberFormatException ne) {
			popMessageAndCloseResultSet("Enter an integer value for Maximum data length", rst);
			maxDataLength.requestFocusInWindow();
			return;
		}

		try {
			// rowsToReturn will be null if all rows are to be returned
			if (btText.isSelected()) {
				tableSpane.setVisible(false);
				textSpane.setVisible(true);

				displayObject.setDisplayParams(rowsToReturn, rst, textOutput, this, maxColWidthValue,
						interColSpaceValue, rowDividers.isSelected(), maxDataLengthValue, nullRep.getText().trim());

				if (enableThreads.isSelected()) {
					new Thread(displayObject, "DISPLAY-RESULT-SET-THREAD").start();
				} else {
					SwingUtilities.invokeLater(displayObject);///////////////////////////////////
				}
			} else if (btTable.isSelected()) {
				textSpane.setVisible(false);
				tableSpane.setVisible(true);
				tableOutput.setMinColWidth(minColWidthValue);
				tableOutput.setMaxColWidth(maxColWidthValue);

				displayGrid.setDisplayParams(rowsToReturn, rst, this, tableOutput, nullRep.getText().trim());

				if (enableThreads.isSelected()) {
					new Thread(displayGrid, "DISPLAY-RESULT-SET-AS-GRID-THREAD").start();
				} else {
					SwingUtilities.invokeLater(displayGrid);///////////////////////////////////
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();/////////////////////////////////////////
			popMessageAndCloseResultSet(e.toString(), rst);
		}
	}

	private void popMessageAndCloseResultSet(String message, ResultSet rst) {
		try {
			popMessage(message);
			rst.close();
		} catch (Exception e) {
			popMessage(e.toString());
		}
	}

	public /* synchronized */ void clearFields() {
		textf1.setText("");
		textf2.setText("");
		textf3.setText("");
		// sqlText.setText("");
		// sqlText.setSelectedItem(makeObj(""));
		passf.setText("");
	}

	public /* synchronized */ void setBusy() {
		busy = true;
		indicatorLabel.setBorder(BorderFactory.createEtchedBorder());
		indicatorLabel.setForeground(Color.red);
		indicatorLabel.setText("BUSY");
		indicatorLabel.setToolTipText("Click to kill the display thread");
	}

	public /* synchronized */ void unsetBusy() {
		busy = false;
		indicatorLabel.setBorder(BorderFactory.createEmptyBorder());
		indicatorLabel.setForeground(Color.green);
		indicatorLabel.setText("Finished");
		indicatorLabel.setToolTipText("");
		final JScrollPane tempSpane = textSpane.isVisible() ? textSpane : tableSpane;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					tempSpane.getVerticalScrollBar().setValue(tempSpane.getVerticalScrollBar().getMaximum());
				} catch (Exception e) {
					System.err.println(e + " from scrollbar thread");
				}
			}
		});
		showNextResult();
	}

	private void showNextResult() {
		if (!busy) {// quit if we are in the middle of displaying a previous
			// resultset. This method will be called again when we are
			// free so all the resultsets _WILL_ eventually be displayed.
			try {
				int updateCount;
				while (true) {// run around in circles until we have no more results to display
					if (stmt.getMoreResults()) {
						if (JOptionPane.showConfirmDialog(null, "Show the next resultset?", "Next?",
								JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
							// System.err.println("Starting next display thread");
							displayResultSet(stmt.getResultSet());
						}
						return;// This resultSet is displayed in a separate thread
						// and this method will be called again when that
						// separate thread quits. So we can exit here.
					} else if ((updateCount = stmt.getUpdateCount()) != -1) {
						popMessage(updateCount + " row(s) updated");
					} else {
						return;// if getMoreResults is false and if the updatecount is -1 then
						// there are no more results to display and we can go home.
					}
				}
			} catch (NullPointerException ne) {
			} catch (SQLException se) {
				popMessage(se.getMessage());
			}
		}
	}

	public /* synchronized */ void showSample1() {
		/*
		 * textf1.setText("sun.jdbc.odbc.JdbcOdbcDriver");
		 * textf2.setText("jdbc:odbc:accesstest"); textf3.setText("scott");
		 * passf.setText(""); sqlText.setText("select * from emp");
		 */
		textf1.setText(ResourceLoader.getResourceString("sample1Driver"));
		textf2.setText(ResourceLoader.getResourceString("sample1ConnectString"));
		textf3.setText(ResourceLoader.getResourceString("sample1User"));
		passf.setText("");
		// sqlText.setText(ResourceLoader.getResourceString("sample1SQLStatement"));
		sqlText.setSelectedItem(makeObj(ResourceLoader.getResourceString("sample1SQLStatement")));
	}

	public /* synchronized */ void showSample2() {
		/*
		 * textf1.setText("oracle.jdbc.driver.OracleDriver");
		 * textf2.setText("jdbc:oracle:thin:@127.0.0.1:1521:orakle");
		 * textf3.setText("scott"); passf.setText("");
		 * sqlText.setText("select * from emp");
		 */
		textf1.setText(ResourceLoader.getResourceString("sample2Driver"));
		textf2.setText(ResourceLoader.getResourceString("sample2ConnectString"));
		textf3.setText(ResourceLoader.getResourceString("sample2User"));
		passf.setText("");
		// sqlText.setText(ResourceLoader.getResourceString("sample2SQLStatement"));
		sqlText.setSelectedItem(makeObj(ResourceLoader.getResourceString("sample2SQLStatement")));
	}

	public /* synchronized */ void setCommit() throws SQLException {
		try {
			conn.setAutoCommit(setCommitButton.isSelected());
		} catch (NullPointerException ne) {
		}
	}

	public void commitTransactions() throws SQLException {
		if (conn != null) {
			conn.commit();
		} else {
			popMessage("Not connected");
		}
	}

	public void rollbackTransactions() throws SQLException {
		if (conn != null) {
			conn.rollback();
		} else {
			popMessage("Not connected");
		}
	}

	private void insertItem(JComboBox combo, String item) {
		int caretPos = ((JTextComponent) sqlText.getEditor().getEditorComponent()).getCaretPosition();
		Object temp = null;
		boolean itemFound = false;
		for (int i = 0; (i < combo.getItemCount()) && (!itemFound); i++) {
			if (combo.getItemAt(i).toString().equals(item.trim())) {
				temp = combo.getItemAt(i);
				combo.removeItemAt(i);
				itemFound = true;
			}
		}
		if (!itemFound)
			temp = makeObj(item.trim());
		combo.insertItemAt(temp, 0);
		combo.setSelectedIndex(0);
		String comboText = ((JTextComponent) sqlText.getEditor().getEditorComponent()).getText();
		caretPos = caretPos > comboText.length() ? comboText.length() : caretPos;
		((JTextComponent) sqlText.getEditor().getEditorComponent()).setCaretPosition(caretPos);
	}

	private Object makeObj(final String item) {
		return new Object() {
			public String toString() {
				return item;
			}
		};
	}

	public /* synchronized */ void executeStatement(String executionCommand) {
		if (!busy) {
			try {
				int updateCount;
				setStatusBarText("");
				if (stmt.execute(executionCommand)) {
					displayResultSet(stmt.getResultSet());
				} else if ((updateCount = stmt.getUpdateCount()) != -1) {
					popMessage(updateCount + " row(s) updated");
				}
				showNextResult();
			} catch (SQLException se) {
				popMessage(se.getMessage());
			} catch (NullPointerException ne) {
				popMessage("Possibly not connected");
			} catch (Exception e) {
				popMessage(e.toString());
			}
		}
	}

	public /* synchronized */ void connectDatabase() throws SQLException, Exception {
		if (!busy) {
			disconnectDatabase();
			conn = driverPanel.getConnection(textf1.getText(), textf2.getText(), textf3.getText(),
					new String(passf.getPassword()));
			indicatorLabel.setText("CONNECTED");
			indicatorLabel.setForeground(Color.green);
			// popMessage("Connected");
			stmt = conn.createStatement();
			setCommit();
		}
	}

	public /* synchronized */ void disconnectDatabase() {
		if (!busy) {
			try {
				stmt.close();
			} catch (NullPointerException ne) {
			} catch (Exception e) {
				popMessage(e.toString());
			}
			// Do not commit automatically on disconnect
//			try {
//				conn.commit();
//			} catch (NullPointerException ne) {
//			} catch (Exception e) {
//				popMessage(e.toString());
//			}
			try {
				conn.close();
			} catch (NullPointerException ne) {
			} catch (Exception e) {
				popMessage(e.toString());
			}
			stmt = null;
			conn = null;
			indicatorLabel.setText("DISCONNECTED");
			indicatorLabel.setForeground(Color.red);
			// popMessage("Disconnected");
		}
	}

	public /* synchronized */ void getTables() throws NullPointerException, SQLException {
		if (!busy) {
			ResultSet rst;
			setStatusBarText("");
			// DatabaseMetaData metaData=conn.getMetaData();
			if (schemaText.isEnabled()) {
				rst = conn.getMetaData().getTables(null, schemaText.getText(), tableText.getText(), null);
			} else {
				rst = conn.getMetaData().getTables(null, null, tableText.getText(), null);
			}
			displayResultSet(rst);
		}
	}

	public /* synchronized */ void getColumns() throws NullPointerException, SQLException {
		if (!busy) {
			ResultSet rst;
			setStatusBarText("");
			// DatabaseMetaData metaData=conn.getMetaData();
			if (schemaText.isEnabled()) {
				rst = conn.getMetaData().getColumns(null, schemaText.getText(), tableText.getText(),
						columnText.getText());
			} else {
				rst = conn.getMetaData().getColumns(null, null, tableText.getText(), columnText.getText());
			}
			displayResultSet(rst);
		}
	}

	public void getSchemas() throws NullPointerException, SQLException {
		if (!busy) {
			setStatusBarText("");
			displayResultSet(conn.getMetaData().getSchemas());
		}
	}

	public void getCatalogs() throws NullPointerException, SQLException {
		if (!busy) {
			setStatusBarText("");
			displayResultSet(conn.getMetaData().getCatalogs());
		}
	}

	private void popMessage(String message) {
		if (btText.isSelected()) {
			tableSpane.setVisible(false);
			textSpane.setVisible(true);
			textOutput.append("\n" + message + "\n");
		}
		JOptionPane.showMessageDialog(null, message);
	}

	private String getSqlText() {
		return ((JTextComponent) sqlText.getEditor().getEditorComponent()).getText();
	}

	public void showSQLFrame(boolean flag) {
		displayTextAreaButton.setSelected(flag);
		if (flag)
			openSqlFrame();
		else
			closeSqlFrame();
	}

	private void closeSqlFrame() {
		// textareaFrame.requestFocus();
		// textareaFrame.setVisible(flag);
		// if(flag)
		// sqlText.setToolTipText("Use the separate window instead of this field to
		// enter SQL statements");
		// else
		// sqlText.setToolTipText("Enter SQL statement and press Enter to execute");
		// sqlText.setEnabled(!flag);
		textareaFrame.closePopup();
		textareaFrame.setVisible(false);
		// sqlText.setToolTipText("Enter SQL statement and press Enter to execute");
		// sqlText.setEnabled(true);
		((JTextComponent) sqlText.getEditor().getEditorComponent()).requestFocusInWindow();
	}

	private void openSqlFrame() {
		sqlText.setPopupVisible(false);
		// sqlText.setToolTipText("Use the separate window instead of this field to
		// enter SQL statements");
		// sqlText.setEnabled(false);
		textareaFrame.setVisible(true);
		textareaFrame.requestFocusInWindow();
	}

	private void enableTextOutputSettings(boolean flag) {
		enableThreads.setSelected(flag);
		minColWidth.setEnabled(!flag);
		minColWidthButton.setEnabled(!flag);
		interColSpace.setEnabled(flag);
		interColSpaceButton.setEnabled(flag);
		maxDataLength.setEnabled(flag);
		maxDataLengthButton.setEnabled(flag);
		clearScreenButton.setEnabled(flag);
		rowDividers.setEnabled(flag);
	}

	public void setStatusBarText(String info) {
		statusBar.setText(info);
	}

	public static void main(String[] args) {
		new SQLMinus();
	}

}
