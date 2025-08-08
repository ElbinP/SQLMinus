package org.misc.sqlminus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class DisplayResultSetAsGrid {

	private Optional<ResultSet> resultSet;
	private Optional<String> executionCommand;
	private Optional<Statement> statement;
	private SQLMinus sqlMinusObject;
	private Optional<Integer> rowsToReturn;
	private String nullRep;
	private volatile boolean busy, stopExecution;
	private SortableTable table;

	public DisplayResultSetAsGrid() {
		busy = false;
		stopExecution = false;
	}

	public void killThread() {
		stopExecution = true;
	}

	public void setDisplayParamsAndRun(Optional<Integer> rowsToReturn, Optional<ResultSet> resultSet,
			Optional<String> executionCommand, Optional<Statement> statement, SQLMinus sqlMinusObject,
			SortableTable table, String nullRep) throws Exception {
		if (!busy) {
			busy = true;
			if (sqlMinusObject != null) {
				sqlMinusObject.setBusy();
			}
			this.stopExecution = false;
			this.table = table;
			this.resultSet = resultSet;
			this.executionCommand = executionCommand;
			this.statement = statement;
			this.sqlMinusObject = sqlMinusObject;
			this.rowsToReturn = rowsToReturn;
			this.nullRep = nullRep;
			table.setNullRep(nullRep);

			run();
		} else {
			throw new Exception("Cannot set display params while displaying a resultset");
		}
	}

	public void run() {
		try {
			ResultSet rst = null;
			try {

				if (executionCommand.isPresent()) {
					if (statement.isPresent()) {
						int updateCount;
						if (statement.get().execute(executionCommand.get())) {
							rst = statement.get().getResultSet();
						} else if ((updateCount = statement.get().getUpdateCount()) != -1) {
							sqlMinusObject.popMessage(updateCount + " row(s) updated");
							return;
						}
					} else {
						throw new SQLMinusException("Statement not available to execute command");
					}
				} else if (resultSet.isPresent()) {
					rst = resultSet.get();
				} else {
					throw new SQLMinusException("Neither execution command nor result set provided");
				}

				int option;
				boolean hasAnotherRow = rst.next();// The result set now points to the first row, if it has one.
				do {
					if (stopExecution)
						throw new ThreadKilledException("Thread killed");
					option = JOptionPane.NO_OPTION;
					if (sqlMinusObject != null)
						sqlMinusObject.clearTextOutput();

					Vector columnHeadings = new Vector();
					// String temp=new String();
					Object temp;

					int columnCount = rst.getMetaData().getColumnCount();
					int[] columnTypes = new int[columnCount];
					for (int i = 1; i <= columnCount; i++) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						temp = rst.getMetaData().getColumnLabel(i);
						/*
						 * if(temp==null){ temp=nullRep; }
						 */
						columnHeadings.add(temp);
						columnTypes[i - 1] = rst.getMetaData().getColumnType(i);
					}

					Vector rowContents = new Vector();
					int rowsRead = 0;

					// rowsToReturn will be null if all rows are to be returned
					while (hasAnotherRow && ((rowsToReturn.isEmpty()) || (rowsRead < rowsToReturn.get().intValue()))) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						Vector row = new Vector();
						for (int i = 1; i <= columnCount; i++) {
							if (stopExecution)
								throw new ThreadKilledException("Thread killed");
							if (DisplayResultSetTableModel
									.getClassForType(columnTypes[i - 1]) == java.lang.String.class)
								temp = rst.getString(i);
							else
								temp = rst.getObject(i);
							/*
							 * if(temp==null){ temp=nullRep; }
							 */
							row.addElement(temp);
						}
						rowContents.addElement(row);
						rowsRead++;
						hasAnotherRow = rst.next();// move on to the next row
					}

					table.changeTable(rowContents, columnHeadings, columnTypes);
					if (sqlMinusObject != null)
						sqlMinusObject.setStatusBarText(" " + rowContents.size() + " row(s) selected");

					if (rowsToReturn.isPresent()) {
						// If there are remaining rows should we display them too?
						if (hasAnotherRow && rowsToReturn.get().intValue() > 0) {
							option = JOptionPane.showConfirmDialog(null,
									"Show the next " + rowsToReturn.get() + " rows?", "Continue?",
									JOptionPane.YES_NO_OPTION);
						}
						if (rowsToReturn.get().intValue() < 1) {
							// textOutput.append("\nYou have set the number of rows to be fetched to
							// "+rowsToReturn.intValue());
							// textOutput.append("\nNo rows will be displayed\n");
							String message = "You have set the number of rows to be fetched to "
									+ rowsToReturn.get().intValue();
							message = message + "\nNo Rows will be displayed";
							JOptionPane.showMessageDialog(null, message);
						}
					}

				} while (option == JOptionPane.YES_OPTION);
				rst.close();
			} catch (ThreadKilledException te) {
				if (rst != null) {
					rst.close();
				}
				JOptionPane.showMessageDialog(null, te.getMessage());
			}
		} catch (SQLException se) {
			// textOutput.append("\n"+se.getMessage()+"\n");
			JOptionPane.showMessageDialog(null, se.getMessage());
		} catch (Exception e) {
			// textOutput.append("\n"+e+"\n");
			System.err.println("Exception from " + this);
			e.printStackTrace();
			System.err.println("End of exception from " + this);
			JOptionPane.showMessageDialog(null, e);
		} finally {
			// System.runFinalization();
			// System.gc();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						table.repaint();
					} catch (Exception e) {
						System.err.println(e + " while trying to repaint the table");
					}
				}
			});
			busy = false;
			// System.err.println("Display thread exiting");
		}

		if (sqlMinusObject != null)
			sqlMinusObject.unsetBusy();
	}

}
