package org.misc.sqlminus.sqlhistory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.misc.sqlminus.SQLFrame;
import org.misc.sqlminus.SQLMinusPreferences;

/**
 * Dialog for searching through SQL history with case-sensitive and regex
 * support. Provides navigation through search results and preview of SQL
 * commands.
 */
public class SQLHistorySearchDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	private static final int MAX_DISPLAY_LENGTH = 60;

	// UI Components
	private JTextField searchField;
	private JCheckBox caseSensitiveCheckbox;
	private JCheckBox regexCheckbox;
	private JList<String> resultsList;
	private DefaultListModel<String> resultsModel;
	private JTextArea previewArea;
	private JLabel statusLabel;
	private JButton searchButton;
	private JButton previousButton;
	private JButton nextButton;
	private JButton loadButton;
	private JButton closeButton;

	// Navigation state
	private int currentResultIndex = -1;

	// Data
	private List<String> sqlHistory;
	private List<SearchResult> searchResults;
	private SQLFrame parentFrame;
	private SQLMinusPreferences sqlMinusPreferences;

	/**
	 * Inner class to hold search result information
	 */
	private static class SearchResult {
		int historyIndex;
		String sqlCommand;
		String displayText;

		SearchResult(int historyIndex, String sqlCommand, String displayText) {
			this.historyIndex = historyIndex;
			this.sqlCommand = sqlCommand;
			this.displayText = displayText;
		}
	}

	/**
	 * Constructor
	 * 
	 * @param parentFrame         The parent SQLFrame
	 * @param sqlHistory          List of SQL commands from history
	 * @param sqlMinusPreferences Application preferences
	 */
	public SQLHistorySearchDialog(SQLFrame parentFrame, List<String> sqlHistory,
			SQLMinusPreferences sqlMinusPreferences) {
		super(parentFrame, "Search SQL History", true);
		this.parentFrame = parentFrame;
		this.sqlHistory = sqlHistory;
		this.sqlMinusPreferences = sqlMinusPreferences;
		this.searchResults = new ArrayList<>();

		initializeUI();
		setupKeyboardShortcuts();

		setSize(700, 600);
		setLocationRelativeTo(parentFrame);
	}

	/**
	 * Initialize the user interface
	 */
	private void initializeUI() {
		setLayout(new BorderLayout(10, 10));

		// Top panel - Search field and options
		JPanel topPanel = new JPanel(new GridBagLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Search label
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		topPanel.add(new JLabel("Search for:"), gbc);

		// Search field
		searchField = new JTextField(30);
		searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					performSearch();
				}
			}
		});
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		topPanel.add(searchField, gbc);

		// Search button
		searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		searchButton.setActionCommand("Search");
		gbc.gridx = 2;
		gbc.weightx = 0;
		topPanel.add(searchButton, gbc);

		// Options panel
		JPanel optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		optionsPanel.add(new JLabel("Options:"));

		caseSensitiveCheckbox = new JCheckBox("Case sensitive");
		optionsPanel.add(caseSensitiveCheckbox);

		regexCheckbox = new JCheckBox("Use regex");
		optionsPanel.add(regexCheckbox);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 3;
		topPanel.add(optionsPanel, gbc);

		add(topPanel, BorderLayout.NORTH);

		// Center panel - Results list and preview
		JPanel centerPanel = new JPanel(new GridBagLayout());
		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.BOTH;

		// Results list
		resultsModel = new DefaultListModel<>();
		resultsList = new JList<>(resultsModel);
		resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		resultsList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					int selectedIndex = resultsList.getSelectedIndex();
					if (selectedIndex >= 0 && selectedIndex < searchResults.size()) {
						currentResultIndex = selectedIndex;
						updatePreview();
						updateButtonStates();
					}
				}
			}
		});

		// Add double-click to load
		resultsList.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					loadSelectedResult();
				}
			}
		});

		JScrollPane resultsScrollPane = new JScrollPane(resultsList);
		resultsScrollPane.setBorder(BorderFactory.createTitledBorder("Results"));
		resultsScrollPane.setPreferredSize(new java.awt.Dimension(650, 200));

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 0.4;
		centerPanel.add(resultsScrollPane, gbc);

		// Preview area
		previewArea = new JTextArea();
		previewArea.setEditable(false);
		previewArea.setLineWrap(true);
		previewArea.setWrapStyleWord(true);
		previewArea.setFont(searchField.getFont());

		JScrollPane previewScrollPane = new JScrollPane(previewArea);
		previewScrollPane.setBorder(BorderFactory.createTitledBorder("Preview"));
		previewScrollPane.setPreferredSize(new java.awt.Dimension(650, 200));

		gbc.gridy = 1;
		gbc.weighty = 0.6;
		centerPanel.add(previewScrollPane, gbc);

		add(centerPanel, BorderLayout.CENTER);

		// Bottom panel - Status and buttons
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

		// Status label
		statusLabel = new JLabel("Enter search term and click Search");
		statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		bottomPanel.add(statusLabel, BorderLayout.NORTH);

		// Buttons panel
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

		previousButton = new JButton("Previous");
		previousButton.addActionListener(this);
		previousButton.setActionCommand("Previous");
		previousButton.setEnabled(false);
		previousButton.setToolTipText("Previous result (Shift+F3)");
		buttonsPanel.add(previousButton);

		nextButton = new JButton("Next");
		nextButton.addActionListener(this);
		nextButton.setActionCommand("Next");
		nextButton.setEnabled(false);
		nextButton.setToolTipText("Next result (F3)");
		buttonsPanel.add(nextButton);

		loadButton = new JButton("Load Selected");
		loadButton.addActionListener(this);
		loadButton.setActionCommand("Load");
		loadButton.setEnabled(false);
		buttonsPanel.add(loadButton);

		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		closeButton.setActionCommand("Close");
		buttonsPanel.add(closeButton);

		bottomPanel.add(buttonsPanel, BorderLayout.CENTER);

		add(bottomPanel, BorderLayout.SOUTH);
	}

	/**
	 * Setup keyboard shortcuts for navigation
	 */
	private void setupKeyboardShortcuts() {
		InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = getRootPane().getActionMap();

		// F3 or Ctrl+G for Next
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "nextResult");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK), "nextResult");
		actionMap.put("nextResult", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (nextButton.isEnabled()) {
					navigateNext();
				}
			}
		});

		// Shift+F3 or Ctrl+Shift+G for Previous
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_DOWN_MASK), "previousResult");
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_G, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK),
				"previousResult");
		actionMap.put("previousResult", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (previousButton.isEnabled()) {
					navigatePrevious();
				}
			}
		});

		// Enter to load selected (when results list has focus)
		resultsList.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "loadSelected");
		resultsList.getActionMap().put("loadSelected", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (loadButton.isEnabled()) {
					loadSelectedResult();
				}
			}
		});

		// Escape to close
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
		actionMap.put("closeDialog", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
	}

	/**
	 * Perform search based on current search term and options
	 */
	private void performSearch() {
		String searchTerm = searchField.getText().trim();

		if (searchTerm.isEmpty()) {
			statusLabel.setText("Please enter a search term");
			statusLabel.setForeground(Color.RED);
			return;
		}

		searchResults.clear();
		resultsModel.clear();
		currentResultIndex = -1;

		try {
			Pattern pattern = buildSearchPattern(searchTerm);

			// Search through history
			// Note: sqlHistory includes working copy at index 0, actual history starts at 1
			// We start from index 1 to skip the working copy and only search actual history
			for (int i = 1; i < sqlHistory.size(); i++) {
				String sql = sqlHistory.get(i);
				Matcher matcher = pattern.matcher(sql);

				if (matcher.find()) {
					// Display position matches the history list position (1-based for first history item)
					// historyIndex is i for correct loading (matches IndexedVector index)
					String displayText = createDisplayText(sql, i);
					SearchResult result = new SearchResult(i, sql, displayText);
					searchResults.add(result);
					resultsModel.addElement(displayText);
				}
			}

			// Update UI based on results
			if (searchResults.isEmpty()) {
				statusLabel.setText("No matches found");
				statusLabel.setForeground(Color.RED);
				previewArea.setText("");
			} else {
				statusLabel.setText(searchResults.size() + " match(es) found");
				statusLabel.setForeground(Color.BLACK);

				// Select first result
				resultsList.setSelectedIndex(0);
				currentResultIndex = 0;
				updatePreview();
			}

			updateButtonStates();

		} catch (PatternSyntaxException e) {
			statusLabel.setText("Invalid regex pattern: " + e.getDescription());
			statusLabel.setForeground(Color.RED);
			searchResults.clear();
			resultsModel.clear();
			previewArea.setText("");
			updateButtonStates();
		}
	}

	/**
	 * Build search pattern based on options
	 */
	private Pattern buildSearchPattern(String searchTerm) throws PatternSyntaxException {
		int flags = 0;

		if (!caseSensitiveCheckbox.isSelected()) {
			flags |= Pattern.CASE_INSENSITIVE;
		}

		if (regexCheckbox.isSelected()) {
			// Use regex as-is
			return Pattern.compile(searchTerm, flags);
		} else {
			// Escape special regex characters for literal search
			String literalPattern = Pattern.quote(searchTerm);
			return Pattern.compile(literalPattern, flags);
		}
	}

	/**
	 * Create display text for result list
	 */
	private String createDisplayText(String sql, int position) {
		// Remove extra whitespace and newlines
		String cleaned = sql.replaceAll("\\s+", " ").trim();

		// Truncate if too long
		if (cleaned.length() > MAX_DISPLAY_LENGTH) {
			cleaned = cleaned.substring(0, MAX_DISPLAY_LENGTH - 3) + "...";
		}

		return String.format("%d. %s", position, cleaned);
	}

	/**
	 * Update preview area with selected result
	 */
	private void updatePreview() {
		if (currentResultIndex >= 0 && currentResultIndex < searchResults.size()) {
			SearchResult result = searchResults.get(currentResultIndex);
			previewArea.setText(result.sqlCommand);
			previewArea.setCaretPosition(0);

			// Update status with current position
			statusLabel.setText(String.format("Result %d of %d", currentResultIndex + 1, searchResults.size()));
			statusLabel.setForeground(Color.BLACK);
		}
	}

	/**
	 * Navigate to next result
	 */
	private void navigateNext() {
		if (searchResults.isEmpty())
			return;

		currentResultIndex = (currentResultIndex + 1) % searchResults.size();
		resultsList.setSelectedIndex(currentResultIndex);
		resultsList.ensureIndexIsVisible(currentResultIndex);
		updatePreview();
		updateButtonStates();
	}

	/**
	 * Navigate to previous result
	 */
	private void navigatePrevious() {
		if (searchResults.isEmpty())
			return;

		currentResultIndex = (currentResultIndex - 1 + searchResults.size()) % searchResults.size();
		resultsList.setSelectedIndex(currentResultIndex);
		resultsList.ensureIndexIsVisible(currentResultIndex);
		updatePreview();
		updateButtonStates();
	}

	/**
	 * Load selected result into parent SQLFrame
	 */
	private void loadSelectedResult() {
		if (currentResultIndex >= 0 && currentResultIndex < searchResults.size()) {
			SearchResult result = searchResults.get(currentResultIndex);
			parentFrame.loadHistoryEntry(result.historyIndex);
			dispose();
		}
	}

	/**
	 * Update button states based on current state
	 */
	private void updateButtonStates() {
		boolean hasResults = !searchResults.isEmpty();
		previousButton.setEnabled(hasResults);
		nextButton.setEnabled(hasResults);
		loadButton.setEnabled(hasResults);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		switch (command) {
		case "Search":
			performSearch();
			break;
		case "Previous":
			navigatePrevious();
			break;
		case "Next":
			navigateNext();
			break;
		case "Load":
			loadSelectedResult();
			break;
		case "Close":
			dispose();
			break;
		}
	}
}
