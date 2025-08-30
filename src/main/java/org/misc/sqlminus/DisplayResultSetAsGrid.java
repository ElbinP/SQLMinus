package org.misc.sqlminus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class DisplayResultSetAsGrid {

	private Optional<String> executionCommand;
	private SQLMinus sqlMinusObject;
	private Optional<Integer> rowsToReturn;
	private String nullRep;
	private final AtomicBoolean busy = new AtomicBoolean(false);
	private final AtomicBoolean stopExecution = new AtomicBoolean(false);
	private SortableTable table;
	private Optional<MetadataRequestEntity> metadataRequestEntity;
	private Optional<Statement> statement;
	private Optional<Connection> connection;

	public void killThread() {
		stopExecution.set(true);
	}

	public void setDisplayParamsAndRun(Optional<Integer> rowsToReturn, Optional<String> executionCommand,
			Optional<Statement> statement, Optional<Connection> connection, SQLMinus sqlMinusObject,
			SortableTable table, String nullRep, Optional<MetadataRequestEntity> metadataRequestEntity)
			throws Exception {
		if (busy.compareAndSet(false, true)) {
			if (sqlMinusObject != null) {
				sqlMinusObject.setBusy();
			}
			this.stopExecution.set(false);
			this.table = table;
			this.executionCommand = executionCommand;
			this.statement = statement;
			this.sqlMinusObject = sqlMinusObject;
			this.rowsToReturn = rowsToReturn;
			this.nullRep = nullRep;
			this.metadataRequestEntity = metadataRequestEntity;
			this.connection = connection;
			table.setNullRep(nullRep);

			run();
		} else {
			throw new Exception("Cannot set display params while displaying a resultset");
		}
	}

	public void run() {
		Optional<Statement> statement = Optional.empty();
		try {
			ResultSet rst = null;
			try {
				if (executionCommand.isPresent()) {
					if (connection.isPresent()) {
						statement = Optional
								.of(StatementFactory.getStatement(connection.get(), executionCommand.get()));
						int updateCount;
						if (statement.get().execute(executionCommand.get())) {
							rst = statement.get().getResultSet();
						} else if ((updateCount = statement.get().getUpdateCount()) != -1) {
							sqlMinusObject.popMessage(updateCount + " row(s) updated");
							return;
						}
					} else {
						throw new SQLMinusException("Not connected");
					}

				} else if (metadataRequestEntity.isPresent()) {
					rst = DisplayResultSetUtil.getMetadataResult(metadataRequestEntity.get(), connection);
				} else {
					throw new SQLMinusException("None of execution command, result set and metadata request provided");
				}

				int option;
				boolean hasAnotherRow = rst.next();// The result set now points to the first row, if it has one.
				do {
					if (stopExecution.get())
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
						if (stopExecution.get())
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
						if (stopExecution.get())
							throw new ThreadKilledException("Thread killed");
						Vector row = new Vector();
						for (int i = 1; i <= columnCount; i++) {
							if (stopExecution.get())
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

			} catch (ThreadKilledException te) {
				JOptionPane.showMessageDialog(null, te.getMessage());
			} finally {
				if (rst != null) {
					rst.close();
				}
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
			busy.set(false);
			// System.err.println("Display thread exiting");
			if (sqlMinusObject != null)
				sqlMinusObject.unsetBusy(statement);
		}

	}

}
