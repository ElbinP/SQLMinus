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
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.plaf.metal.MetalComboBoxEditor;
import javax.swing.text.JTextComponent;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.misc.sqlminus.MetadataRequestEntity.MetadataRequestType;
import org.misc.sqlminus.session.SessionsPanel;

import nocom.special.CustomizedMouseAdapter;
import nocom.special.ImageReader;
import nocom.special.LookAndFeelMenu;
import nocom.special.UtilityFunctions;

public class SQLMinus extends JFrame implements ActionListener {

	private static final long serialVersionUID = -3452359178949290219L;
	private final int DEFAULT_MINCOLWIDTH = 15, DEFAULT_MAXCOLWIDTH = 50, DEFAULT_INTERCOLSPACE = 4,
			DEFAULT_MAXDATALENGTH = 1000;
	private final String COMMIT_TRANSACTIONS_COMMAND = "COMMIT_TRANSACTIONS";
	private final String ROLLBACK_TRANSACTIONS_COMMAND = "ROLLBACK_TRANSACTIONS";
	public CustomizedMouseAdapter commonAdapter = new CustomizedMouseAdapter(false);
	private RSyntaxTextArea textOutput;
	private SQLFrame textareaFrame;
	private JTextField driverConnectionString, dbUsername, tableText, columnText, schemaText, catalogText, statusBar;
	private JComboBox<String> sqlText;
	private JTextField minColWidth, maxColWidth, interColSpace, maxDataLength, nullRep;
	private JButton minColWidthButton, maxColWidthButton, interColSpaceButton, maxDataLengthButton;
	private JPasswordField dbPassword;
	private JButton indicatorLabel;
	private JLabel schemaLabel, catalogLabel;
	private JSplitPane splitPane;
	private Optional<Connection> conn = Optional.empty();
	private JCheckBox displayTextAreaButton, clearScreenButton, setCommitButton, rowDividers, enableThreads;
	private JComboBox<String> rowsComboBox;
	private DisplayResultSet displayObject = new DisplayResultSet();
	private DisplayResultSetAsGrid displayGrid = new DisplayResultSetAsGrid();
	private ClassLoaderPanel driverPanel;
	private final AtomicBoolean busy = new AtomicBoolean(false);
	private SortableTable tableOutput;
	private JRadioButton btText, btTable;
	private JScrollPane tableSpane;
	private RTextScrollPane textSpane;
	private String[] rowsComboBoxOptions = { "100", "500", "All" };
	private SQLMinusPreferences sqlMinusPreferences = new SQLMinusPreferences();
	public LookAndFeelMenu laf = new LookAndFeelMenu(new Component[] {}, KeyEvent.VK_L, null, sqlMinusPreferences);
	private static final int SPLIT_PANE_DIVIDER_LOCATION = 142;
	private static final int NUMBER_OF_COMMANDS_TO_SAVE_IN_DROPDOWN = 50;

	/************** The Constructor for SQLMinus ***********************/

	public SQLMinus() {
		super("SQL Minus");

		GridBagLayout gridbag = new GridBagLayout();

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
		Color buttonColor = null;
		Color buttonTextColor = null;
		Image iconImage = new ImageIcon().getImage();
		try {
			iconImage = ImageReader.getImage(this.getClass(), "/images/sqlminus.png");
		} catch (Exception e) {
		}
		// try{iconImage=new
		// ImageReader().getImage("SQLMinus.jar","/images/icon.png");}catch(Exception
		// e){}
		laf.addComponentToMonitor(commonAdapter.getPopupMenu());

		/************ The Connection panel components ************/

		JPanel connectionPanel = new JPanel(gridbag);

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(8, 8, 8, 8);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;

		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel label2 = new JLabel("Connect String", JLabel.RIGHT);
		// label2.setFont(f);
		gridbag.setConstraints(label2, c);
		connectionPanel.add(label2);

		c.gridx = 1;
		c.gridwidth = 4;
		c.weightx = 1;
		driverConnectionString = new JTextField(sqlMinusPreferences.get(Constants.PreferencesKeys.CONNECT_STRING, ""),
				50);
		driverConnectionString.setActionCommand("CONNECT");
		driverConnectionString.addActionListener(this);
		driverConnectionString.addMouseListener(commonAdapter);
		// driverConnectionString.setFont(tfont);
		gridbag.setConstraints(driverConnectionString, c);
		connectionPanel.add(driverConnectionString);

		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 0.1;
		c.gridwidth = 1;
		JLabel label3 = new JLabel("Username", JLabel.RIGHT);
		// label3.setFont(f);
		gridbag.setConstraints(label3, c);
		connectionPanel.add(label3);

		c.gridx = 1;
		c.weightx = 1;
		dbUsername = new JTextField(sqlMinusPreferences.get(Constants.PreferencesKeys.DB_USERNAME, ""), 20);
		dbUsername.setActionCommand("CONNECT");
		dbUsername.addActionListener(this);
		dbUsername.addMouseListener(commonAdapter);
		// dbUsername.setFont(tfont);
		gridbag.setConstraints(dbUsername, c);
		connectionPanel.add(dbUsername);

		c.gridx = 2;
		c.weightx = 0.1;
		JLabel label4 = new JLabel("Password", JLabel.RIGHT);
		// label4.setFont(f);
		gridbag.setConstraints(label4, c);
		connectionPanel.add(label4);

		c.gridx = 3;
		c.weightx = 1;
		c.gridwidth = 2;
		dbPassword = new JPasswordField(
				sqlMinusPreferences.getDecryptedValue(Constants.PreferencesKeys.DB_PASSWORD, ""), 20);
		dbPassword.setActionCommand("CONNECT");
		dbPassword.addActionListener(this);
		dbPassword.addMouseListener(commonAdapter);
		// dbPassword.setFont(tfont);
		dbPassword.setEchoChar('*');
		UtilityFunctions.fixPasswordFieldKeyBindings(dbPassword);
		gridbag.setConstraints(dbPassword, c);
		connectionPanel.add(dbPassword);

		c.gridx = 1;
		c.gridy = 2;
		c.gridwidth = 1;
		JButton clearFieldsButton = new JButton("CLEAR FIELDS");
		clearFieldsButton.addActionListener(this);
		// button2.setFont(f);
		clearFieldsButton.setBackground(buttonColor);
		clearFieldsButton.setForeground(buttonTextColor);
		gridbag.setConstraints(clearFieldsButton, c);
		connectionPanel.add(clearFieldsButton);

		c.gridx = 3;
		JButton connectButton = new JButton("CONNECT");
		connectButton.addActionListener(this);
		// button3.setFont(f);
		connectButton.setBackground(buttonColor);
		connectButton.setForeground(buttonTextColor);
		gridbag.setConstraints(connectButton, c);
		connectionPanel.add(connectButton);

		c.gridx = 4;
		c.weightx = 1;
		JButton disconnectButton = new JButton("DISCONNECT");
		disconnectButton.addActionListener(this);
		// disconnectButton.setFont(f);
		disconnectButton.setBackground(buttonColor);
		disconnectButton.setForeground(buttonTextColor);
		gridbag.setConstraints(disconnectButton, c);
		connectionPanel.add(disconnectButton);

		c.gridx = 5;
		c.gridy = 0;
		c.gridheight = 4;
		c.fill = GridBagConstraints.BOTH;
		JPanel sessionsPanel = new SessionsPanel(driverConnectionString, dbUsername, dbPassword, sqlMinusPreferences);
		gridbag.setConstraints(sessionsPanel, c);
		connectionPanel.add(sessionsPanel);

		connectionPanel.setFocusTraversalPolicy(new CustomFocusTraversalPolicy(List.of(driverConnectionString,
				dbUsername, dbPassword, clearFieldsButton, connectButton, disconnectButton)));
		connectionPanel.setFocusTraversalPolicyProvider(true);
		connectionPanel.setBackground(backgroundColor);

		/*****************
		 * Connection Panel components end here******** now the optionsPanel is added
		 ***************************************************************/

		JPanel optionsPanel = new JPanel(gridbag);

		c.gridx = -1;
		c.gridy = -1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 10;
		c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
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

		clearScreenButton = new JCheckBox("Clear screen before each result",
				sqlMinusPreferences.getBoolean(Constants.PreferencesKeys.CLEAR_SCREEN_BEFORE_EACH_RESULT, true));
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
		rowsComboBox = new JComboBox<>(rowsComboBoxOptions);
		rowsComboBox.setEditable(true);
		String rowsToSelectUserPreference = sqlMinusPreferences.get(Constants.PreferencesKeys.ROWS_TO_SELECT, null);
		if (rowsToSelectUserPreference != null) {
			if (!(Arrays.stream(rowsComboBoxOptions).anyMatch(rowsToSelectUserPreference::equalsIgnoreCase))) {
				rowsComboBox.addItem(rowsToSelectUserPreference);
			}
			rowsComboBox.setSelectedItem(rowsToSelectUserPreference);
		}
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

		enableThreads = new JCheckBox("Enable threads",
				sqlMinusPreferences.getBoolean(Constants.PreferencesKeys.ENABLE_THREADS, false));
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
		// btTable.setFont(f);
		btTable.addActionListener(this);
		btText = new JRadioButton("Output as text");
		// btText.setFont(f);
		btText.addActionListener(this);
		ButtonGroup btGroup = new ButtonGroup();
		btGroup.add(btTable);
		btGroup.add(btText);
		if (sqlMinusPreferences.getBoolean(Constants.PreferencesKeys.OUTPUT_IN_GRID, true)) {
			btTable.setSelected(true);
		} else {
			btText.setSelected(true);
		}
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
		maxColWidth = new JTextField(
				"" + sqlMinusPreferences.getInt(Constants.PreferencesKeys.MAX_COL_WIDTH, DEFAULT_MAXCOLWIDTH));
		maxColWidth.setToolTipText("Value must be greater than 0");
		// maxColWidth.setFont(tfont);
		maxColWidth.addMouseListener(commonAdapter);
		maxColWidthButton = new JButton("Maximum column width :");
		maxColWidthButton.setToolTipText("Click to reset to default value");
		maxColWidthButton.addActionListener(this);
		// maxColWidthButton.setFont(f);

		minColWidth = new JTextField(
				"" + sqlMinusPreferences.getInt(Constants.PreferencesKeys.MIN_COL_WIDTH, DEFAULT_MINCOLWIDTH));
		minColWidth.setToolTipText("Value must be 0 or greater");
		// minColWidth.setFont(tfont);
		minColWidth.addMouseListener(commonAdapter);
		minColWidthButton = new JButton("Minimum column width :");
		minColWidthButton.setToolTipText("Click to reset to default value");
		minColWidthButton.addActionListener(this);
		// minColWidthButton.setFont(f);

		interColSpace = new JTextField(
				"" + sqlMinusPreferences.getInt(Constants.PreferencesKeys.INTER_COLUMN_SPACING, DEFAULT_INTERCOLSPACE));
		interColSpace.setToolTipText("Value must be 0 or greater");
		// interColSpace.setFont(tfont);
		interColSpace.addMouseListener(commonAdapter);
		interColSpaceButton = new JButton("Inter-column spacing :");
		interColSpaceButton.setToolTipText("Click to reset to default value");
		interColSpaceButton.addActionListener(this);
		// interColSpaceButton.setFont(f);

		maxDataLength = new JTextField(
				"" + sqlMinusPreferences.getInt(Constants.PreferencesKeys.MAX_DATA_LENGTH, DEFAULT_MAXDATALENGTH));
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
		nullRep = new JTextField(sqlMinusPreferences.get(Constants.PreferencesKeys.NULL_REPRESENTATION, "<NULL>"));
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

		rowDividers = new JCheckBox("Always show row dividers",
				sqlMinusPreferences.getBoolean(Constants.PreferencesKeys.ALWAYS_SHOW_ROW_DIVIDERS, false));
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
		getTableButton.setForeground(buttonTextColor);
		gridbag.setConstraints(getTableButton, c);
		miscPanel.add(getTableButton);

		c.gridwidth = GridBagConstraints.REMAINDER;
		JButton getColumnButton = new JButton("GET COLUMNS");
		getColumnButton.addActionListener(this);
		// getColumnButton.setFont(f);
		getColumnButton.setToolTipText("Displays the matching columns in the matching tables and their descriptions");
		getColumnButton.setBackground(buttonColor);
		getColumnButton.setForeground(buttonTextColor);
		gridbag.setConstraints(getColumnButton, c);
		miscPanel.add(getColumnButton);

		c.gridwidth = 1;
		c.weighty = 0;
		catalogLabel = new JLabel("Catalog name", JLabel.RIGHT);
		catalogLabel.setEnabled(false);
		// dummy6.setFont(f);
		gridbag.setConstraints(catalogLabel, c);
		miscPanel.add(catalogLabel);

		// --------------------------------------------------------------------------------------------------------
		// c.weightx=2;
		catalogText = new JTextField("", 20);
		catalogText.setEnabled(false);
		// schemaText.setFont(tfont);
		catalogText.addMouseListener(commonAdapter);
		JCheckBox catalogBox = new JCheckBox("Enable", false);
		// schemaBox.setFont(f);
		catalogBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				boolean isEnabled = ((JCheckBox) ae.getSource()).isSelected();
				catalogLabel.setEnabled(isEnabled);
				catalogText.setEnabled(isEnabled);
			}
		});
		JPanel catalogPanel = new JPanel();
		catalogPanel.setLayout(new BoxLayout(catalogPanel, BoxLayout.X_AXIS));
		catalogPanel.setBorder(BorderFactory.createEtchedBorder());
		catalogPanel.add(catalogBox);
		catalogPanel.add(catalogText);
		gridbag.setConstraints(catalogPanel, c);
		miscPanel.add(catalogPanel);
		// ------------------------------------------------------------------------------------------------------

		JLabel dummy8 = new JLabel("");
		// dummy8.setFont(f);
		gridbag.setConstraints(dummy8, c);
		miscPanel.add(dummy8);

		JButton getSchemaButton = new JButton("GET SCHEMAS");
		getSchemaButton.addActionListener(this);
		// getSchemaButton.setFont(f);
		getSchemaButton.setToolTipText("Displays all the schemas");
		getSchemaButton.setBackground(buttonColor);
		getSchemaButton.setForeground(buttonTextColor);
		gridbag.setConstraints(getSchemaButton, c);
		miscPanel.add(getSchemaButton);

		c.gridwidth = GridBagConstraints.REMAINDER;
		JButton getCatalogButton = new JButton("GET CATALOGS");
		getCatalogButton.addActionListener(this);
		// getCatalogButton.setFont(f);
		getCatalogButton.setToolTipText("Displays all the catalogs");
		getCatalogButton.setBackground(buttonColor);
		getCatalogButton.setForeground(buttonTextColor);
		gridbag.setConstraints(getCatalogButton, c);
		miscPanel.add(getCatalogButton);

		miscPanel.setBackground(backgroundColor);

		/******************
		 * miscPanel components end here***************** now its the turn of the
		 * driverPanel
		 ***********/

		driverPanel = new ClassLoaderPanel(laf, c.insets, backgroundColor, buttonColor, buttonTextColor, f, tfont);

		/****************
		 * driverPanel components end here******************** now the Tabbed Pane is
		 * created
		 ************************/

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.RIGHT);
		tabbedPane.addTab("Connection", connectionPanel);
		tabbedPane.addTab("Options", optionsPanel);
		tabbedPane.addTab("Display", displayFormatPanel);
		tabbedPane.addTab("Metadata", miscPanel);
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
		String[] optionArray = {};
		sqlText = new JComboBox<>(optionArray);
		loadSQLTextsFromPreferences();
		MetalComboBoxEditor editor = new MetalComboBoxEditor();
		sqlText.setEditor(editor);
		sqlText.setEditable(true);
		sqlText.setToolTipText("Enter SQL statement and press Enter to execute");
		sqlText.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ke) {
				if ((ke.getKeyCode() == KeyEvent.VK_ENTER) || ((ke.getKeyCode() == KeyEvent.VK_E)
						&& ((ke.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0))) {
					insertItem(sqlText, getSqlText());
					executeStatement(getSqlText());
				} else if ((ke.getKeyCode() == KeyEvent.VK_W)
						&& ((ke.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)) {
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

		textOutput = new RSyntaxTextArea();
		textOutput.setHighlightCurrentLine(false);
		textOutput.setFont(tfont);
		// textOutput.setSelectionColor(Color.white);
		// textOutput.setSelectionColor(new Color(180,180,180));
		textOutput.setEditable(false);
		textOutput.setBackground(backgroundLight);
		CustomizedMouseAdapter textOutputAdapter = new CustomizedMouseAdapter(true);
		laf.addComponentToMonitor(textOutputAdapter.getPopupMenu());
		textOutput.addMouseListener(textOutputAdapter);

		textSpane = new RTextScrollPane(textOutput);
		textSpane.setLineNumbersEnabled(false);

		c.weighty = 2000;
		c.weightx = 2000;
		c.fill = GridBagConstraints.BOTH;
		c.gridheight = GridBagConstraints.RELATIVE;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(textSpane, c);
		bottomPanel.add(textSpane);

		// adding a jtable to the bottompanel(to be used instead of the text area if so
		// desired
		tableOutput = new SortableTable(new DisplayResultSetTableModel(), DEFAULT_MINCOLWIDTH, DEFAULT_MAXCOLWIDTH,
				backgroundLight, iconImage, tfont);
		tableOutput.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableOutput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableOutput.setBackground(backgroundColor);
		tableOutput.setFont(tfont);
		tableOutput.setCellEditorFont(tfont);
		laf.addComponentToMonitor(tableOutput.getCellEditorPopupMenu());
		laf.addComponentToMonitor(tableOutput.getContentsViewFrame());
		laf.addComponentToMonitor(tableOutput.getContentsViewPopupMenu());
		laf.addComponentToMonitor(tableOutput.getExcelFileChooser());
		laf.addComponentToMonitor(tableOutput.getPopup());
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
		splitPane.setDividerLocation(SPLIT_PANE_DIVIDER_LOCATION);
		splitPane.setBackground(backgroundColor);
		getContentPane().add(splitPane);

		/**************
		 * Now creating the initially invisible frame for entering SQL statements,the
		 * text area inside, the popup menu for the said text area and such.
		 ************************************************************/

		textareaFrame = new SQLFrame(this, tfont, f, backgroundLight, 0, 0, sqlMinusPreferences);
		textareaFrame.getContentPane().setBackground(backgroundColor);
		textareaFrame.setIconImage(iconImage);
		{
			int windowHeight = sqlMinusPreferences.getInt(Constants.PreferencesKeys.SQLFRAME_WINDOW_HEIGHT, 150);
			int windowWidth = sqlMinusPreferences.getInt(Constants.PreferencesKeys.SQLFRAME_WINDOW_WIDTH, 100);
			int windowX = sqlMinusPreferences.getInt(Constants.PreferencesKeys.SQLFRAME_WINDOW_X, 700);
			int windowY = sqlMinusPreferences.getInt(Constants.PreferencesKeys.SQLFRAME_WINDOW_Y, 300);
			textareaFrame.setBounds(windowX, windowY, windowWidth, windowHeight);
		}

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
					splitPane.setDividerLocation(SPLIT_PANE_DIVIDER_LOCATION);
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
		setMacDockIcon(iconImage);
		setJMenuBar(menuBar);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				exitSystem();
			}
		});
		pack();
		{
			int windowHeight = sqlMinusPreferences.getInt(Constants.PreferencesKeys.WINDOW_HEIGHT, 950);
			int windowWidth = sqlMinusPreferences.getInt(Constants.PreferencesKeys.WINDOW_WIDTH, 700);
			int windowX = sqlMinusPreferences.getInt(Constants.PreferencesKeys.WINDOW_X, 10);
			int windowY = sqlMinusPreferences.getInt(Constants.PreferencesKeys.WINDOW_Y, 10);

			setBounds(windowX, windowY, windowWidth, windowHeight);
		}

		setKeyboardShortcutToOpenQueryWindow();

		enableTextOutputSettings(btText.isSelected());
		setVisible(true);
		dbPassword.requestFocusInWindow();

		laf.setSavedLookAndFeel();
	}

	public static void main(String[] args) {
		new SQLMinus();
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
			else if (command.equals("Minimum column width :"))
				minColWidth.setText("" + DEFAULT_MINCOLWIDTH);
			else if (command.equals("Maximum column width :"))
				maxColWidth.setText("" + DEFAULT_MAXCOLWIDTH);
			else if (command.equals("Inter-column spacing :"))
				interColSpace.setText("" + DEFAULT_INTERCOLSPACE);
			else if (command.equals("Maximum data length :"))
				maxDataLength.setText("" + DEFAULT_MAXDATALENGTH);
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
				saveSQLTextsToPreferences();
			} else if (command.equals("SHOW QUERY WINDOW-COMBO COMMAND")) {
				showSQLFrame(true);
			}
		} catch (Exception e) {
			popMessage(UtilityFunctions.getExceptionMessages(e));
		}
	}

	public /* synchronized */ void exitSystem() {
		// Do not automatically commit on exit
//		try {
//			conn.commit();
//		} catch (Exception e) {
//		}
		try {
			if (conn.isPresent()) {
				conn.get().close();
			}
		} catch (Exception e) {
		}
		saveWindowSizeAndPositionInPreferences();
		System.exit(0);
	}

	public /* synchronized */ void clearTextOutput() {
		sqlMinusPreferences.putBoolean(Constants.PreferencesKeys.CLEAR_SCREEN_BEFORE_EACH_RESULT,
				clearScreenButton.isSelected());
		if (clearScreenButton.isSelected()) {
			textOutput.setText("");
		}
	}

	public void displayResultSet(Optional<String> executionCommand,
			Optional<MetadataRequestEntity> metadataRequestEntity) {
		displayResultSet(executionCommand, metadataRequestEntity, Optional.empty());
	}

	public /* synchronized */ void displayResultSet(Optional<String> executionCommand,
			Optional<MetadataRequestEntity> metadataRequestEntity, Optional<Statement> statement) {
		Optional<Integer> rowsToReturn = Optional.empty();
		// rowsToReturn will be null if all rows are to be returned
		if (!(((String) rowsComboBox.getSelectedItem()).equalsIgnoreCase("All"))) {
			try {
				String selectedItemValue = ((String) rowsComboBox.getSelectedItem()).trim();
				rowsToReturn = Optional.of(Integer.valueOf(selectedItemValue));
				sqlMinusPreferences.put(Constants.PreferencesKeys.ROWS_TO_SELECT, selectedItemValue);
			} catch (NumberFormatException ne) {
				popMessage("Enter an integer for the number of records to be returned");
				rowsComboBox.requestFocusInWindow();
				return;
			}
		} else {
			sqlMinusPreferences.put(Constants.PreferencesKeys.ROWS_TO_SELECT, "All");
		}

		int minColWidthValue = DEFAULT_MINCOLWIDTH;
		try {
			if (minColWidth.isEnabled()) {
				minColWidthValue = Integer.parseInt(minColWidth.getText().trim());
				if (minColWidthValue < 0) {
					popMessage("Maximum column width should be 0 or greater");
					minColWidth.requestFocusInWindow();
					return;
				} else {
					sqlMinusPreferences.putInt(Constants.PreferencesKeys.MIN_COL_WIDTH, minColWidthValue);
				}
			}
		} catch (NumberFormatException ne) {
			popMessage("Enter an integer value for Minimum column width");
			minColWidth.requestFocusInWindow();
			return;
		}

		int maxColWidthValue = DEFAULT_MAXCOLWIDTH;
		try {
			if (maxColWidth.isEnabled()) {
				maxColWidthValue = Integer.parseInt(maxColWidth.getText().trim());
				if (maxColWidthValue < 1) {
					popMessage("Maximum column width should be greater than 0");
					maxColWidth.requestFocusInWindow();
					return;
				} else {
					sqlMinusPreferences.putInt(Constants.PreferencesKeys.MAX_COL_WIDTH, maxColWidthValue);
				}
			}
		} catch (NumberFormatException ne) {
			popMessage("Enter an integer value for Maximum column width");
			maxColWidth.requestFocusInWindow();
			return;
		}

		int interColSpaceValue = DEFAULT_INTERCOLSPACE;
		try {
			if (interColSpace.isEnabled()) {
				interColSpaceValue = Integer.parseInt(interColSpace.getText().trim());
				if (interColSpaceValue < 0) {
					popMessage("Inter-column spacing should be 0 or greater");
					interColSpace.requestFocusInWindow();
					return;
				} else {
					sqlMinusPreferences.putInt(Constants.PreferencesKeys.INTER_COLUMN_SPACING, interColSpaceValue);
				}
			}
		} catch (NumberFormatException ne) {
			popMessage("Enter an integer value for Inter-column spacing");
			interColSpace.requestFocusInWindow();
			return;
		}

		int maxDataLengthValue = DEFAULT_MAXDATALENGTH;
		try {
			if (maxDataLength.isEnabled()) {
				maxDataLengthValue = Integer.parseInt(maxDataLength.getText().trim());
				if (maxDataLengthValue < 1) {
					popMessage("Maximum data length should be greater than 0");
					maxDataLength.requestFocusInWindow();
					return;
				} else {
					sqlMinusPreferences.putInt(Constants.PreferencesKeys.MAX_DATA_LENGTH, maxDataLengthValue);
				}
			}
		} catch (NumberFormatException ne) {
			popMessage("Enter an integer value for Maximum data length");
			maxDataLength.requestFocusInWindow();
			return;
		}

		sqlMinusPreferences.put(Constants.PreferencesKeys.NULL_REPRESENTATION, nullRep.getText());
		sqlMinusPreferences.putBoolean(Constants.PreferencesKeys.ALWAYS_SHOW_ROW_DIVIDERS, rowDividers.isSelected());
		sqlMinusPreferences.putBoolean(Constants.PreferencesKeys.ENABLE_THREADS, enableThreads.isSelected());
		sqlMinusPreferences.putBoolean(Constants.PreferencesKeys.OUTPUT_IN_GRID, btTable.isSelected());

		final Optional<Integer> rowsToReturnFinal = rowsToReturn;
		final int maxColWidthValueFinal = maxColWidthValue;
		final int interColSpaceValueFinal = interColSpaceValue;
		final int maxDataLengthValueFinal = maxDataLengthValue;

		try {
			// rowsToReturn will be null if all rows are to be returned
			if (btText.isSelected()) {
				tableSpane.setVisible(false);
				textSpane.setVisible(true);

				Thread displayThread = new Thread(() -> {
					try {
						displayObject.setDisplayParamsAndRun(rowsToReturnFinal, executionCommand, statement, conn,
								textOutput, this, maxColWidthValueFinal, interColSpaceValueFinal,
								rowDividers.isSelected(), maxDataLengthValueFinal, nullRep.getText(),
								metadataRequestEntity);

					} catch (Exception e) {
						popMessage(e.toString());
					}
				}, "DISPLAY-RESULT-SET-THREAD");

				if (enableThreads.isSelected()) {
					displayThread.start();
				} else {
					SwingUtilities.invokeLater(displayThread);///////////////////////////////////
				}
			} else if (btTable.isSelected()) {
				textSpane.setVisible(false);
				tableSpane.setVisible(true);
				tableOutput.setMinColWidth(minColWidthValue);
				tableOutput.setMaxColWidth(maxColWidthValue);

				Thread displayThread = new Thread(() -> {
					try {
						displayGrid.setDisplayParamsAndRun(rowsToReturnFinal, executionCommand, statement, conn, this,
								tableOutput, nullRep.getText(), metadataRequestEntity);
					} catch (Exception e) {
						popMessage(e.toString());
					}
				}, "DISPLAY-RESULT-SET-AS-GRID-THREAD");

				if (enableThreads.isSelected()) {
					displayThread.start();
				} else {
					SwingUtilities.invokeLater(displayThread);///////////////////////////////////
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();/////////////////////////////////////////
			popMessage(e.toString());
		}
	}

	public /* synchronized */ void clearFields() {
		driverConnectionString.setText("");
		dbUsername.setText("");
		// sqlText.setText("");
		// sqlText.setSelectedItem(makeObj(""));
		dbPassword.setText("");
		saveConnectionSettingsToPreferences();
	}

	private void saveConnectionSettingsToPreferences() {
		sqlMinusPreferences.put(Constants.PreferencesKeys.CONNECT_STRING, driverConnectionString.getText());
		sqlMinusPreferences.put(Constants.PreferencesKeys.DB_USERNAME, dbUsername.getText());
		sqlMinusPreferences.putEncryptedValue(Constants.PreferencesKeys.DB_PASSWORD,
				new String(dbPassword.getPassword()));
	}

	public /* synchronized */ void setBusy() {
		busy.set(true);
		;
		indicatorLabel.setBorder(BorderFactory.createEtchedBorder());
		indicatorLabel.setForeground(Color.red);
		indicatorLabel.setText("BUSY");
		indicatorLabel.setToolTipText("Click to kill the display thread");
	}

	public /* synchronized */ void unsetBusy(Optional<Statement> stmt) {
		busy.set(false);
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
		stmt.ifPresent(this::showNextResult);
	}

	private void showNextResult(Statement stmt) {
		if (!busy.get()) {// quit if we are in the middle of displaying a previous
			// resultset. This method will be called again when we are
			// free so all the resultsets _WILL_ eventually be displayed.

			displayResultSet(Optional.empty(), Optional.empty(), Optional.of(stmt));
		}
	}

	public /* synchronized */ void setCommit() throws SQLException {
		if (conn.isPresent()) {
			conn.get().setAutoCommit(setCommitButton.isSelected());
		}
	}

	public void commitTransactions() throws SQLException {
		if (conn.isPresent()) {
			conn.get().commit();
		} else {
			popMessage("Not connected");
		}
	}

	public void rollbackTransactions() throws SQLException {
		if (conn.isPresent()) {
			conn.get().rollback();
		} else {
			popMessage("Not connected");
		}
	}

	private void insertItem(JComboBox<String> combo, String item) {
		int caretPos = ((JTextComponent) sqlText.getEditor().getEditorComponent()).getCaretPosition();
		String temp = null;
		boolean itemFound = false;
		for (int i = 0; (i < combo.getItemCount()) && (!itemFound); i++) {
			if (combo.getItemAt(i).toString().equals(item.trim())) {
				temp = combo.getItemAt(i);
				combo.removeItemAt(i);
				itemFound = true;
			}
		}
		if (!itemFound)
			temp = item.trim();
		combo.insertItemAt(temp, 0);
		combo.setSelectedIndex(0);
		String comboText = ((JTextComponent) sqlText.getEditor().getEditorComponent()).getText();
		caretPos = caretPos > comboText.length() ? comboText.length() : caretPos;
		((JTextComponent) sqlText.getEditor().getEditorComponent()).setCaretPosition(caretPos);
		Thread preferencesThread = new Thread(() -> saveSQLTextsToPreferences());
		preferencesThread.start();
	}

	private synchronized void saveSQLTextsToPreferences() {
		try {
			StringBuilder sqlTextsString = new StringBuilder();
			// save only the latest N sql texts in preferences
			for (int i = 0; i < sqlText.getItemCount() && i < NUMBER_OF_COMMANDS_TO_SAVE_IN_DROPDOWN; i++) {
				sqlTextsString.append(sqlText.getItemAt(i).toString()).append("<br/>");
			}
			String encryptedSqlTexts = sqlMinusPreferences.getEncryptedString(sqlTextsString.toString());
			sqlMinusPreferences.put(Constants.PreferencesKeys.SQL_TEXTS, encryptedSqlTexts);
		} catch (SQLMinusException e) {
			popMessage(e.getMessage());
		}
	}

	private void loadSQLTextsFromPreferences() {
		try {
			String sqlTextsString = sqlMinusPreferences.get(Constants.PreferencesKeys.SQL_TEXTS, null);
			if (sqlTextsString != null) {
				String decryptedSqlText = sqlMinusPreferences.getDecryptedString(sqlTextsString);
				if (decryptedSqlText.trim().length() > 0) {
					var decryptedTexts = Arrays.asList(decryptedSqlText.split("<br/>"));
					initialiseSqlText(sqlText, decryptedTexts);
				}
			}
		} catch (Exception e) {
			popMessage(e.getMessage());
		}
	}

	private void initialiseSqlText(JComboBox<String> sqlText, List<String> decryptedTexts) {
		decryptedTexts.forEach(t -> sqlText.addItem(t));
	}

	public /* synchronized */ void executeStatement(String executionCommand) {
		if (conn.isPresent()) {
			if (!busy.get()) {
				try {
					setStatusBarText("");
					displayResultSet(Optional.of(executionCommand), Optional.empty());
				} catch (Exception e) {
					popMessage(UtilityFunctions.getExceptionMessages(e));
				}
			}
		} else {
			popMessage("Not connected");
		}
	}

	public /* synchronized */ void connectDatabase() throws MalformedURLException, SQLException {
		if (!busy.get()) {
			disconnectDatabase();
			saveConnectionSettingsToPreferences();
			conn = Optional.of(driverPanel.getConnection(driverConnectionString.getText(), dbUsername.getText(),
					new String(dbPassword.getPassword())));
			indicatorLabel.setText("CONNECTED");
			indicatorLabel.setForeground(Color.green);
			// popMessage("Connected");
			setCommit();
			sqlText.requestFocusInWindow();
		}
	}

	public /* synchronized */ void disconnectDatabase() {
		if (!busy.get()) {
			try {
				if (conn.isPresent() && !conn.get().getAutoCommit()) {
					conn.get().rollback();
				}
			} catch (Exception e) {
				popMessage(e.toString());
			}

			try {
				if (conn.isPresent()) {
					conn.get().close();
					conn = Optional.empty();
				}
				indicatorLabel.setText("DISCONNECTED");
				indicatorLabel.setForeground(Color.red);
			} catch (Exception e) {
				popMessage(e.toString());
			}
			// popMessage("Disconnected");
		}
	}

	public /* synchronized */ void getTables() throws SQLException {
		if (conn.isPresent()) {
			if (!busy.get()) {
				setStatusBarText("");

				String catalogPattern = null;
				if (catalogText.isEnabled()) {
					catalogPattern = catalogText.getText();
				}
				String schemaPattern = null;
				if (schemaText.isEnabled()) {
					schemaPattern = schemaText.getText();
				}
				MetadataRequestEntity entity = new MetadataRequestEntity(catalogPattern, schemaPattern,
						tableText.getText(), null, MetadataRequestType.TABLES);

				displayResultSet(Optional.empty(), Optional.of(entity));
			}
		} else {
			popMessage("Not connected");
		}
	}

	public /* synchronized */ void getColumns() throws SQLException {
		if (conn.isPresent()) {
			if (!busy.get()) {
				setStatusBarText("");

				String catalogPattern = null;
				if (catalogText.isEnabled()) {
					catalogPattern = catalogText.getText();
				}
				String schemaPattern = null;
				if (schemaText.isEnabled()) {
					schemaPattern = schemaText.getText();
				}

				MetadataRequestEntity entity = new MetadataRequestEntity(catalogPattern, schemaPattern,
						tableText.getText(), columnText.getText(), MetadataRequestType.COLUMNS);

				displayResultSet(Optional.empty(), Optional.of(entity));
			}
		} else {
			popMessage("Not connected");
		}
	}

	public void getSchemas() throws SQLException {
		if (conn.isPresent()) {
			if (!busy.get()) {
				setStatusBarText("");

				String catalogPattern = null;
				if (catalogText.isEnabled()) {
					catalogPattern = catalogText.getText();
				}
				String schemaPattern = null;
				if (schemaText.isEnabled()) {
					schemaPattern = schemaText.getText();
				}
				MetadataRequestEntity entity = new MetadataRequestEntity(catalogPattern, schemaPattern, null, null,
						MetadataRequestType.SCHEMAS);

				displayResultSet(Optional.empty(), Optional.of(entity));
			}
		} else {
			popMessage("Not connected");
		}
	}

	public void getCatalogs() throws SQLException {
		if (conn.isPresent()) {
			if (!busy.get()) {
				setStatusBarText("");
				MetadataRequestEntity entity = new MetadataRequestEntity(null, null, null, null,
						MetadataRequestType.CATALOGS);
				displayResultSet(Optional.empty(), Optional.of(entity));
			}
		} else {
			popMessage("Not connected");
		}
	}

	public void popMessage(String message) {
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

	private void saveWindowSizeAndPositionInPreferences() {
		sqlMinusPreferences.putInt(Constants.PreferencesKeys.WINDOW_HEIGHT,
				((Double) getBounds().getHeight()).intValue());
		sqlMinusPreferences.putInt(Constants.PreferencesKeys.WINDOW_WIDTH,
				((Double) getBounds().getWidth()).intValue());
		sqlMinusPreferences.putInt(Constants.PreferencesKeys.WINDOW_X, ((Double) getBounds().getX()).intValue());
		sqlMinusPreferences.putInt(Constants.PreferencesKeys.WINDOW_Y, ((Double) getBounds().getY()).intValue());
		sqlMinusPreferences.putInt(Constants.PreferencesKeys.SQLFRAME_WINDOW_HEIGHT,
				((Double) textareaFrame.getBounds().getHeight()).intValue());
		sqlMinusPreferences.putInt(Constants.PreferencesKeys.SQLFRAME_WINDOW_WIDTH,
				((Double) textareaFrame.getBounds().getWidth()).intValue());
		sqlMinusPreferences.putInt(Constants.PreferencesKeys.SQLFRAME_WINDOW_X,
				((Double) textareaFrame.getBounds().getX()).intValue());
		sqlMinusPreferences.putInt(Constants.PreferencesKeys.SQLFRAME_WINDOW_Y,
				((Double) textareaFrame.getBounds().getY()).intValue());
		sqlMinusPreferences.put(Constants.PreferencesKeys.SQLFRAME_TOOLBAR_POSITION,
				textareaFrame.getToolbarPosition());
		sqlMinusPreferences.putInt(Constants.PreferencesKeys.SQLFRAME_TOOLBAR_ORIENTATION,
				textareaFrame.getToolbarOrientation());
		sqlMinusPreferences.putBoolean(Constants.PreferencesKeys.SQLFRAME_WORD_WRAP, textareaFrame.getWordWrap());
	}

	private void setMacDockIcon(Image iconImage) {
		try {
			// Java 9+ approach via reflection
			Class<?> appClass = Class.forName("java.awt.Taskbar");
			Object taskbar = appClass.getDeclaredMethod("getTaskbar").invoke(null);
			Method setIconMethod = appClass.getDeclaredMethod("setIconImage", Image.class);
			setIconMethod.invoke(taskbar, iconImage);
		} catch (Exception e) {
			// Fallback or log error
			System.err.println("Unable to set Dock icon: " + e);
		}
	}

	private void setKeyboardShortcutToOpenQueryWindow() {
		InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getRootPane().getActionMap();
		KeyStroke ctrlW = KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		inputMap.put(ctrlW, "openQueryWindow");
		actionMap.put("openQueryWindow", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showSQLFrame(!textareaFrame.isVisible());
			}
		});
	}
}
