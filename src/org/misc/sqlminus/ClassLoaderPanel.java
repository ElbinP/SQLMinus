package org.misc.sqlminus;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import nocom.special.LookAndFeelMenu;

public class ClassLoaderPanel extends JPanel implements ActionListener {

	DefaultListModel listModel;
	JList driversList;
	JFileChooser fileChooser;
	JButton addButton, removeButton;

	public ClassLoaderPanel(LookAndFeelMenu laf, Insets insets, Color backgroundColor, Color buttonColor, Font f,
			Font tfont) {
		super();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		fileChooser = new JFileChooser(System.getProperty("user.dir"));
		fileChooser.addChoosableFileFilter(new CustomFileFilter());
		laf.addComponentToMonitor(fileChooser);

		listModel = new DefaultListModel();
		listModel.addElement(ResourceLoader.getResourceString("jarFile1"));
		listModel.addElement(ResourceLoader.getResourceString("jarFile2"));
		driversList = new JList(listModel);
		driversList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// driversList.setFont(tfont);
		if (listModel.getSize() > 0) {
			driversList.setSelectedIndex(listModel.getSize() - 1);
		}

		c.insets = insets;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 10;
		c.weighty = 10;
		JScrollPane listScrollPane = new JScrollPane(driversList);
		gridbag.setConstraints(listScrollPane, c);
		add(listScrollPane);

		JPanel buttonsPanel = new JPanel(new GridLayout(2, 1, 16, 16));
		addButton = new JButton("Add");
		addButton.setToolTipText("Add a jar or zip file that contains the necessary driver classes");
		addButton.setBackground(buttonColor);
		addButton.setForeground(Color.white);
		// addButton.setFont(f);
		addButton.addActionListener(this);
		buttonsPanel.add(addButton);
		removeButton = new JButton("Remove");
		removeButton.setToolTipText("Remove the selected file from the list");
		removeButton.setBackground(buttonColor);
		removeButton.setForeground(Color.white);
		// removeButton.setFont(f);
		removeButton.addActionListener(this);
		buttonsPanel.add(removeButton);

		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		buttonsPanel.setBackground(backgroundColor);
		gridbag.setConstraints(buttonsPanel, c);
		add(buttonsPanel);

		setBackground(backgroundColor);
	}

	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand().equals("Add")) {
			int returnValue = fileChooser.showDialog(this, "Add");
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				String s = fileChooser.getSelectedFile().getPath();
				if (new File(s).exists()) {
					listModel.addElement(s);
					if (listModel.getSize() > 0) {
						driversList.setSelectedIndex(listModel.getSize() - 1);
					}
				}
			}
		}
		if (ae.getActionCommand().equals("Remove")) {
			if (driversList.getSelectedIndex() != -1) {
				listModel.removeElementAt(driversList.getSelectedIndex());
				if (listModel.getSize() > 0) {
					driversList.setSelectedIndex(listModel.getSize() - 1);
				}
			}
		}
	}

	public Connection getConnection(String driverName, String connectString, String username, String password)
			throws Exception {
		URL[] url = new URL[listModel.getSize()];
		for (int i = 0; i < listModel.getSize(); i++) {
			url[i] = new URL("file:" + (String) listModel.getElementAt(i));
		}
		URLClassLoader classLoader = new URLClassLoader(url);
		Driver driver = (Driver) Class.forName(driverName, true, classLoader).newInstance();
		Properties prop = new Properties();
		prop.setProperty("user", username);
		prop.setProperty("password", password);
		Connection conn;
		try {
			if ((conn = driver.connect(connectString, prop)) != null)
				return conn;
			else
				throw new SQLException("Invalid connect string");
		} catch (NullPointerException ne) {
			throw new SQLException("Invalid connect string");
		}
	}

}
