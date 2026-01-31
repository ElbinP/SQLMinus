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

import org.misc.sqlminus.DisplayResultSetUtil.ExecutionResult;

public class DisplayResultSetAsGrid {

	/**
	 * Callback interface for pagination events
	 */
	public interface PaginationCallback {
		void onMoreRowsAvailable(int batchSize, int currentRowCount);
		void onLoadingComplete();
	}

	private Optional<String> executionCommand;
	private PaginationCallback paginationCallback;
	private Optional<ResultSet> pendingResultSet = Optional.empty();
	private int totalRowsDisplayed = 0;
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

	public void setPaginationCallback(PaginationCallback callback) {
		this.paginationCallback = callback;
	}

	public void continueLoading(int batchSize) {
		if (pendingResultSet.isPresent()) {
			try {
				ResultSet rst = pendingResultSet.get();
				boolean hasAnotherRow = true;
				int rowsRead = 0;
				Vector rowContents = new Vector();
				
				int columnCount = rst.getMetaData().getColumnCount();
				int[] columnTypes = new int[columnCount];
				for (int i = 1; i <= columnCount; i++) {
					columnTypes[i - 1] = rst.getMetaData().getColumnType(i);
				}
				
				while (hasAnotherRow && rowsRead < batchSize) {
					if (stopExecution.get()) break;
					Vector row = new Vector();
					for (int i = 1; i <= columnCount; i++) {
						Object temp;
						if (DisplayResultSetTableModel.getClassForType(columnTypes[i - 1]) == java.lang.String.class)
							temp = rst.getString(i);
						else
							temp = rst.getObject(i);
						row.addElement(temp);
					}
					rowContents.addElement(row);
					rowsRead++;
					hasAnotherRow = rst.next();
				}
				
				// Append to existing table using appendTable method
				final int previousRowCount = table.getRowCount();
				table.appendTable(rowContents, columnTypes);
				
				totalRowsDisplayed += rowsRead;
				if (sqlMinusObject != null) {
					sqlMinusObject.setStatusBarText(" " + totalRowsDisplayed + " row(s) selected");
				}
				
				table.repaint();
				
				// Auto-scroll to show newly added rows at the end
				SwingUtilities.invokeLater(() -> {
					try {
						// Scroll to the last row to show all newly added rows
						int lastRow = table.getRowCount() - 1;
						if (lastRow >= 0) {
							table.scrollRectToVisible(table.getCellRect(lastRow, 0, true));
						}
					} catch (Exception e) {
						// Ignore scroll errors
					}
				});
				
				if (hasAnotherRow && paginationCallback != null) {
					paginationCallback.onMoreRowsAvailable(batchSize, totalRowsDisplayed);
				} else {
					cleanupPendingResultSet();
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error loading next batch: " + e.getMessage());
				cleanupPendingResultSet();
			}
		}
	}

	public void stopLoading() {
		stopExecution.set(true);
		cleanupPendingResultSet();
	}

	private void cleanupPendingResultSet() {
		if (pendingResultSet.isPresent()) {
			try {
				pendingResultSet.get().close();
			} catch (SQLException e) {
				System.err.println("Error closing pending ResultSet: " + e.getMessage());
			}
			pendingResultSet = Optional.empty();
		}
		if (paginationCallback != null) {
			paginationCallback.onLoadingComplete();
		}
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
		Optional<Statement> stmtInternal = Optional.empty();
		Optional<ResultSet> rstOptional = Optional.empty();
		try {
			ExecutionResult result = DisplayResultSetUtil.getResult(executionCommand, statement, metadataRequestEntity,
					connection, sqlMinusObject, Optional.empty(), true);
			stmtInternal = result.statement();
			rstOptional = result.resultSet();
			if (rstOptional.isPresent()) {
				ResultSet rst = rstOptional.get();
				totalRowsDisplayed = 0;
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
					totalRowsDisplayed += rowContents.size();
					if (sqlMinusObject != null)
						sqlMinusObject.setStatusBarText(" " + totalRowsDisplayed + " row(s) selected");

					if (rowsToReturn.isPresent()) {
						// If there are remaining rows, notify via callback instead of modal dialog
						if (hasAnotherRow && rowsToReturn.get().intValue() > 0) {
							pendingResultSet = Optional.of(rst);
							if (paginationCallback != null) {
								paginationCallback.onMoreRowsAvailable(rowsToReturn.get(), totalRowsDisplayed);
							}
							// Exit loop - will resume when continueLoading() is called
							return;
						}
						if (rowsToReturn.get().intValue() < 1) {
							String message = "You have set the number of rows to be fetched to "
									+ rowsToReturn.get().intValue();
							message = message + "\nNo Rows will be displayed";
							JOptionPane.showMessageDialog(null, message);
						}
					}
					
					// No more rows or no pagination - cleanup
					if (paginationCallback != null) {
						paginationCallback.onLoadingComplete();
					}

				} while (false); // Changed from option == YES_OPTION since we now use callbacks

			}

		} catch (ThreadKilledException te) {
			JOptionPane.showMessageDialog(null, te.getMessage());
		} catch (SQLException | SQLMinusException se) {
			// textOutput.append("\n"+se.getMessage()+"\n");
			JOptionPane.showMessageDialog(null, se.getMessage());
		} catch (Exception e) {
			// textOutput.append("\n"+e+"\n");
			System.err.println("Exception from " + this);
			e.printStackTrace();
			System.err.println("End of exception from " + this);
			JOptionPane.showMessageDialog(null, e);
		} finally {

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						table.repaint();
					} catch (Exception e) {
						System.err.println(e + " while trying to repaint the table");
					}
				}
			});
			// Only close ResultSet if it's not pending for pagination
			if (rstOptional.isPresent() && pendingResultSet.isEmpty()) {
				try {
					rstOptional.get().close();
				} catch (SQLException e) {
					System.err.println(e + " while closing result set");
				}
			}
			busy.set(false);
			// System.err.println("Display thread exiting");
			if (sqlMinusObject != null)
				sqlMinusObject.unsetBusy(stmtInternal);
		}

	}

}
