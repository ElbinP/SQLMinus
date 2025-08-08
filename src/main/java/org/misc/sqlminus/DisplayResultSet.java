package org.misc.sqlminus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import nocom.special.UtilityFunctions;

public class DisplayResultSet {

	private JTextArea textOutput;
	private Optional<ResultSet> resultSet;
	private Optional<String> executionCommand;
	private Optional<Statement> statement;
	private SQLMinus sqlMinusObject;
	private Optional<Integer> rowsToReturn;
	private int maxColWidth, spacing;
	private boolean rowDividers;
	private int maxDataLength;
	private String nullRep;
	private volatile boolean busy, stopExecution;

	public DisplayResultSet() {
		busy = false;
		stopExecution = false;
	}

	public void killThread() {
		stopExecution = true;
	}

	public void setDisplayParamsAndRun(Optional<Integer> rowsToReturn, Optional<ResultSet> resultSet,
			Optional<String> executionCommand, Optional<Statement> statement, JTextArea textOutput,
			SQLMinus sqlMinusObject, int maxColWidth, int spacing, boolean rowDividers, int maxDataLength,
			String nullRep) throws Exception {
		if (!busy) {
			busy = true;
			if (sqlMinusObject != null)
				sqlMinusObject.setBusy();
			this.stopExecution = false;
			this.textOutput = textOutput;
			this.resultSet = resultSet;
			this.executionCommand = executionCommand;
			this.statement = statement;
			this.sqlMinusObject = sqlMinusObject;
			this.rowsToReturn = rowsToReturn;
			this.maxColWidth = maxColWidth;
			this.spacing = spacing;
			this.rowDividers = rowDividers;
			this.maxDataLength = maxDataLength;
			this.nullRep = nullRep;
			// SwingUtilities.invokeLater(this);
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

					/*
					 * What's being done here is this: There is a master arraylist. The master
					 * arraylist stores the row arraylists. The row arraylists stores the individual
					 * column values.
					 */

					ArrayList masterList = new ArrayList();// masterList is the master arraylist

					// This section reads and stores the column headings
					int columnCount = rst.getMetaData().getColumnCount();
					// String[] columnName=new
					// String[columnCount+1];//++++++++++++++++++++++++++++++++++++++++++++++++++++++++
					Vector[] columnName = new Vector[columnCount + 1];
					for (int i = 1; i <= columnCount; i++) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						// columnName[i]=rst.getMetaData().getColumnName(i);//++++++++++++++++++++++++++++++++++++++++++++++
						String temp = rst.getMetaData().getColumnLabel(i);
						if (temp == null) {
							temp = nullRep;
						}
						temp = UtilityFunctions.truncateString(temp, maxDataLength).replace('\n', ' ')
								.replace('\t', ' ').replace('\r', ' ');
						columnName[i] = UtilityFunctions.splitUpString(temp, maxColWidth);
					}

					int rowsRead = 0;

					// rowsToReturn will be null if all rows are to be returned
					while (hasAnotherRow && ((rowsToReturn.isEmpty()) || (rowsRead < rowsToReturn.get().intValue()))) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						ArrayList arlRow = new ArrayList();// arraylist for the corresponding row
						for (int i = 1; i <= columnCount; i++) {
							if (stopExecution)
								throw new ThreadKilledException("Thread killed");
							String temp = rst.getString(i);
							if (temp == null) {
								temp = nullRep;
							}
							temp = UtilityFunctions.truncateString(temp, maxDataLength).replace('\n', ' ')
									.replace('\t', ' ').replace('\r', ' ');
							arlRow.add(temp);// add each column value to the row arraylist
						}
						masterList.add(arlRow);// add the row arraylist to the master arraylist
						arlRow = null;
						rowsRead++;
						hasAnotherRow = rst.next();// move on to the next row
					}

					Object[] row = masterList.toArray();
					int rowlength = row.length;
					masterList = null;

					// String[][] columnValue=new
					// String[rowlength+1][columnCount+1];//++++++++++++++++++++++++++++++++
					Vector[][] columnValue = new Vector[rowlength + 1][columnCount + 1];
					for (int i = 1; i <= rowlength; i++) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						Object[] temp = ((ArrayList) row[i - 1]).toArray();
						row[i - 1] = null;
						for (int j = 1; j <= columnCount; j++) {
							if (stopExecution)
								throw new ThreadKilledException("Thread killed");
							// columnValue[i][j]=(String)temp[j-1];//++++++++++++++++++++++++++++++++++++++++++
							columnValue[i][j] = UtilityFunctions.splitUpString((String) temp[j - 1], maxColWidth);
						}
						temp = null;
					}
					row = null;

					// This section determines the maximum size an entry in a column will have
					int[] columnSize = new int[columnCount + 1];
					for (int i = 1; i <= columnCount; i++) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						// columnSize[i]=columnName[i].length();//+++++++++++++++++++++++++++++++++++++++++++++
						columnSize[i] = UtilityFunctions
								.getMaxLength((String[]) columnName[i].toArray(new String[columnName[i].size()]));
					}
					for (int j = 1; j <= columnCount; j++) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						for (int i = 1; i <= rowlength; i++) {
							if (stopExecution)
								throw new ThreadKilledException("Thread killed");
							try {
								// if(columnSize[j]<columnValue[i][j].length())
								// columnSize[j]=columnValue[i][j].length();//+++++++++++++++++++++++++++++
								if (columnSize[j] < UtilityFunctions.getMaxLength(
										(String[]) columnValue[i][j].toArray(new String[columnValue[i][j].size()]))) {
									columnSize[j] = UtilityFunctions.getMaxLength(
											(String[]) columnValue[i][j].toArray(new String[columnValue[i][j].size()]));
								}
							} catch (NullPointerException ne) {
							}
						}
					}

					int maxRowSize = 0;
					for (int i = 1; i <= columnCount; i++) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						maxRowSize = maxRowSize + columnSize[i] + spacing;
					}

					int columnNameMaxRows = 1;
					for (int i = 1; i <= columnCount; i++) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						if (columnNameMaxRows < columnName[i].size()) {
							columnNameMaxRows = columnName[i].size();
						}
					}

					int[] columnValueMaxRows = new int[rowlength + 1];
					for (int i = 1; i <= rowlength; i++) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						columnValueMaxRows[i] = 1;
						for (int j = 1; j <= columnCount; j++) {
							if (stopExecution)
								throw new ThreadKilledException("Thread killed");
							if (columnValueMaxRows[i] < columnValue[i][j].size()) {
								columnValueMaxRows[i] = columnValue[i][j].size();
							}
						}
					}

					int maxRowRows = 1;
					for (int i = 1; i <= rowlength; i++) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						if (maxRowRows < columnValueMaxRows[i]) {
							maxRowRows = columnValueMaxRows[i];
						}
					}

					// The column headings are always displayed even when there are no rows to
					// display
					// textOutput.append("\n");//+++++++++++++++++++++++++++++++++++++++++
					for (int m = 1; m <= columnNameMaxRows; m++) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						textOutput.append("\n");
						for (int i = 1; i <= columnCount; i++) {
							if (stopExecution)
								throw new ThreadKilledException("Thread killed");
							int temp = 0;
							try {
								textOutput.append(((String) columnName[i].elementAt(m - 1)));
								temp = ((String) columnName[i].elementAt(m - 1)).length();
							} catch (ArrayIndexOutOfBoundsException ae) {
								// ArrayIndexOutOfBoundsException will be thrown when there is no elementAt(m-1)
							}
							for (int j = 1; j <= (columnSize[i] - temp) + spacing; j++) {
								if (stopExecution)
									throw new ThreadKilledException("Thread killed");
								textOutput.append(" ");
							}
						}
					}
					textOutput.append("\n");
					for (int i = 1; i <= columnCount; i++) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						// for(int j=1;j<=columnName[i].length();j++)
						// textOutput.append("_");//+++++++++++++++++++++++
						for (int j = 1; j <= UtilityFunctions.getMaxLength(
								(String[]) columnName[i].toArray(new String[columnName[i].size()])); j++) {
							if (stopExecution)
								throw new ThreadKilledException("Thread killed");
							textOutput.append("_");
						}
						// for(int j=1;j<=(columnSize[i]-columnName[i].length())+spacing;j++)
						// textOutput.append(" ");//++++++++++++++++
						for (int j = 1; j <= (columnSize[i]
								- UtilityFunctions.getMaxLength(
										(String[]) columnName[i].toArray(new String[columnName[i].size()]))
								+ spacing); j++) {
							if (stopExecution)
								throw new ThreadKilledException("Thread killed");
							textOutput.append(" ");
						}
					}

					// This section displays the actual row contents
					// textOutput.append("\n");//+++++++++++++++++++++++++++++++++++++++++++++
					for (int i = 1; i <= rowlength; i++) {
						if (stopExecution)
							throw new ThreadKilledException("Thread killed");
						for (int m = 1; m <= columnValueMaxRows[i]; m++) {
							if (stopExecution)
								throw new ThreadKilledException("Thread killed");
							textOutput.append("\n");
							for (int j = 1; j <= columnCount; j++) {
								if (stopExecution)
									throw new ThreadKilledException("Thread killed");
								int temp = 0;
								try {
									textOutput.append(((String) columnValue[i][j].elementAt(m - 1)));
									temp = ((String) columnValue[i][j].elementAt(m - 1)).length();
								} catch (ArrayIndexOutOfBoundsException ae) {
									// ArrayIndexOutOfBoundsException will be thrown when there is no elementAt(m-1)
								}
								for (int k = 1; k <= (columnSize[j] - temp) + spacing; k++) {
									if (stopExecution)
										throw new ThreadKilledException("Thread killed");
									textOutput.append(" ");
								}
								// columnValue[i][j]=null;
							}
						}
						if (rowDividers || maxRowRows > 1) {
							textOutput.append("\n");
							for (int p = 1; p <= maxRowSize; p++) {
								if (stopExecution)
									throw new ThreadKilledException("Thread killed");
								textOutput.append("-");
							}
						}
					}
					textOutput.append("\n\n" + rowlength + " row(s) selected\n");
					if (sqlMinusObject != null)
						sqlMinusObject.setStatusBarText(" " + rowlength + " row(s) selected");

					if (rowsToReturn.isPresent()) {
						// If there are remaining rows should we display them too?
						if (hasAnotherRow && rowsToReturn.get().intValue() > 0) {
							option = JOptionPane.showConfirmDialog(null,
									"Show the next " + rowsToReturn.get() + " rows?", "Continue?",
									JOptionPane.YES_NO_OPTION);
						}
						if (rowsToReturn.get().intValue() < 1) {
							textOutput.append("\nYou have set the number of rows to be fetched to "
									+ rowsToReturn.get().intValue());
							textOutput.append("\nNo rows will be displayed\n");
						}
					}

				} while (option == JOptionPane.YES_OPTION);
				rst.close();
			} catch (ThreadKilledException te) {
				if (rst != null) {
					rst.close();
				}
				textOutput.append("\n\n" + te.getMessage() + "\n");
			}
		} catch (SQLException se) {
			textOutput.append("\n\n" + se.getMessage() + "\n");
		} catch (Exception e) {
			textOutput.append("\n\n" + e + "\n");
		} finally {
			// System.runFinalization();
			// System.gc();
			busy = false;
		}

		if (sqlMinusObject != null)
			sqlMinusObject.unsetBusy();
	}

}
