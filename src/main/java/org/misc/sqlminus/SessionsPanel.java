package org.misc.sqlminus;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class SessionsPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -8070131233453907610L;
	private JTextField sessionName;
	private JList<String> sessionsList;
	private JButton loadButton, saveButton, deleteButton;
	private final JTextField driverClassName, connectionString, userName;
	private final JPasswordField password;
	private final SQLMinusPreferences sqlMinusPreferences;

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
		JScrollPane scrollPane = new JScrollPane(sessionsList);
		add(scrollPane, c);

		// Buttons in right column (rows 2, 3, 4)
		c.gridx = 1;
		c.gridheight = 1;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;

		c.gridy = 2;
		loadButton = new JButton("Load");
		loadButton.addActionListener(this);
		add(loadButton, c);

		c.gridy = 3;
		saveButton = new JButton("Save");
		saveButton.addActionListener(this);
		add(saveButton, c);

		c.gridy = 4;
		c.anchor = GridBagConstraints.NORTHWEST;
		deleteButton = new JButton("Delete");
		deleteButton.addActionListener(this);
		add(deleteButton, c);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == loadButton) {
			System.out.println("Load clicked");
			// Implement load logic
		} else if (e.getSource() == saveButton) {
			System.out.println("Save clicked");
			// Implement save logic
		} else if (e.getSource() == deleteButton) {
			System.out.println("Delete clicked");
			// Implement delete logic
		}
	}
}
