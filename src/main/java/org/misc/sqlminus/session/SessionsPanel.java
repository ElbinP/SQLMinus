package org.misc.sqlminus.session;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.misc.sqlminus.SQLMinusException;
import org.misc.sqlminus.SQLMinusPreferences;

public class SessionsPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -8070131233453907610L;
	private JTextField sessionName;
	private JList<String> sessionsList;
	private JButton loadButton, saveButton, deleteButton;
	private final JTextField driverClassName, connectionString, userName;
	private final JPasswordField password;
	private final SQLMinusPreferences sqlMinusPreferences;
	private static final String SESSION_ERROR_TITLE = "Session error";

	public SessionsPanel(JTextField driverClassName, JTextField connectionString, JTextField userName,
			JPasswordField password, SQLMinusPreferences sqlMinusPreferences) {
		super();
		this.driverClassName = driverClassName;
		this.connectionString = connectionString;
		this.userName = userName;
		this.password = password;
		this.sqlMinusPreferences = sqlMinusPreferences;
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createTitledBorder("Load, save or delete a stored session"));

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1, 1, 1, 1);

		// Row 0: Label
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		add(new JLabel("Saved session"), c);

		// Row 1: Text field
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		sessionName = new JTextField(20);
		add(sessionName, c);

		// Row 2: List with scroll
		c.gridy = 2;
		c.gridheight = 3;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		sessionsList = new JList<>();
		sessionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		DefaultListModel<String> model = new DefaultListModel<>();
		sessionsList.setModel(model);
		JScrollPane scrollPane = new JScrollPane(sessionsList);
		add(scrollPane, c);

		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridy = 1;
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		add(saveButton, c);
		
		c.gridy = 2;
		loadButton = new JButton("Load");
		c.anchor = GridBagConstraints.NORTHWEST;
		loadButton.addActionListener(this);
		add(loadButton, c);


		c.gridy = 3;
		c.anchor = GridBagConstraints.NORTHWEST;
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(this);
		add(deleteButton, c);

		loadSessionsListFromPreferences();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == loadButton) {
			loadSession();
		} else if (e.getSource() == saveButton) {
			saveSession();
		} else if (e.getSource() == deleteButton) {
			deleteSession();
		}
	}

	private void loadSessionsListFromPreferences() {
		try {
			DefaultListModel<String> model = (DefaultListModel<String>) sessionsList.getModel();
			List<String> sessions = sqlMinusPreferences.getSessionsList();
			Collections.sort(sessions);
			model.clear();
			sessions.forEach(s -> model.addElement(s));
		} catch (SQLMinusException e) {
			popErrorMessage(e.getMessage());
		}
	}

	private void saveSession() {
		if (sessionName.getText().trim().isBlank()) {
			popErrorMessage("Specify the session name to save");
		} else {
			SessionEntity sessionEntity = SessionEntity.builder().driverClassName(driverClassName.getText())
					.password(new String(password.getPassword())).connectionString(connectionString.getText())
					.userName(userName.getText()).build();
			try {
				sqlMinusPreferences.putSession(sessionName.getText().trim(), sessionEntity);
				sessionName.setText("");
				loadSessionsListFromPreferences();
			} catch (SQLMinusException e1) {
				popErrorMessage(e1.getMessage());
			}
		}
	}

	private void loadSession() {
		if (sessionsList.getSelectedIndex() == -1) {
			popErrorMessage("Select session to load");
		} else {
			try {
				SessionEntity sessionEntity = sqlMinusPreferences.getSession(sessionsList.getSelectedValue());
				driverClassName.setText(sessionEntity.getDriverClassName());
				connectionString.setText(sessionEntity.getConnectionString());
				userName.setText(sessionEntity.getUserName());
				password.setText(sessionEntity.getPassword());
			} catch (SQLMinusException e) {
				popErrorMessage(e.getMessage());
			}
		}
	}

	private void deleteSession() {
		if (sessionsList.getSelectedIndex() == -1) {
			popErrorMessage("Select session to delete");
		} else {
			sqlMinusPreferences.deleteSession(sessionsList.getSelectedValue());
			loadSessionsListFromPreferences();
		}
	}

	private void popErrorMessage(String message) {
		JOptionPane.showMessageDialog(null, message, SESSION_ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
	}

}
