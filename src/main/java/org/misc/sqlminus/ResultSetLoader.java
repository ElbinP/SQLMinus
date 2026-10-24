package org.misc.sqlminus;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.misc.sqlminus.DisplayResultSetUtil.ExecutionFeedback;
import org.misc.sqlminus.DisplayResultSetUtil.ExecutionResult;

/**
 * Runs SQL statements and metadata requests and feeds their result sets to a {@link ResultSetRenderer}. All JDBC
 * work happens on a {@link SwingWorker} thread. The public methods must be called on the event dispatch thread, and
 * the {@link Listener} is notified on it.
 * <p>
 * When a row limit is set, a result set with more rows stays open until the remaining rows are loaded with
 * {@link #loadNext} or abandoned with {@link #stop}. Only then does the loader move on to the statement's next
 * result.
 */
public class ResultSetLoader {

	public interface Listener {
		void loadingStarted();

		void rowsLoaded(int totalRows);

		void moreRowsAvailable(int batchSize, int totalRows);

		void loadingFinished();
	}

	@FunctionalInterface
	private interface Step {
		void run(LoadWorker worker) throws Exception;
	}

	private final Listener listener;
	private final AtomicBoolean cancelled = new AtomicBoolean(false);
	private boolean busy;

	// Owned by the worker while busy and by the event dispatch thread otherwise. SwingWorker.execute() and done()
	// order the hand-offs between the two.
	private Optional<Connection> connection = Optional.empty();
	private ResultSetRenderer renderer;
	private Optional<Integer> rowLimit = Optional.empty();
	private Optional<Statement> statement = Optional.empty();
	private Optional<ResultSet> pendingResultSet = Optional.empty();
	private List<String> columnLabels;
	private int[] columnTypes;
	private int totalRows;
	private int lastBatchSize;

	public ResultSetLoader(Listener listener) {
		this.listener = listener;
	}

	public boolean isBusy() {
		return busy;
	}

	/**
	 * Runs either a SQL command or a metadata request. Any result still pending from a previous execution is
	 * discarded.
	 */
	public void execute(Optional<String> command, Optional<MetadataRequestEntity> metadataRequest,
			Optional<Connection> connection, ResultSetRenderer renderer, Optional<Integer> rowLimit) {
		if (busy) {
			throw new IllegalStateException("Cannot execute while a result set is loading");
		}
		close();
		this.connection = connection;
		this.renderer = renderer;
		this.rowLimit = rowLimit;
		start(worker -> {
			ExecutionResult result = DisplayResultSetUtil.getResult(command, Optional.empty(), metadataRequest,
					this.connection, worker);
			statement = result.statement();
			if (result.resultSet().isPresent()) {
				worker.fetchFirstBatch(result.resultSet().get());
			}
		});
	}

	public void loadNext(int batchSize) {
		if (busy || pendingResultSet.isEmpty()) {
			return;
		}
		ResultSet rst = pendingResultSet.get();
		pendingResultSet = Optional.empty();
		start(worker -> worker.fetchBatch(rst, Optional.of(batchSize), false));
	}

	/**
	 * Abandons the rest of the pending result set and moves on to the statement's next result, if any.
	 */
	public void stop() {
		if (busy || pendingResultSet.isEmpty()) {
			return;
		}
		closeQuietly(pendingResultSet.get());
		pendingResultSet = Optional.empty();
		start(worker -> {
		});
	}

	/**
	 * Abandons the current load and any further results. Takes effect before the next row is read.
	 */
	public void cancel() {
		if (busy) {
			cancelled.set(true);
		}
	}

	/**
	 * Closes the pending result set and statement. Has no effect while a result set is loading.
	 */
	public void close() {
		if (!busy) {
			releaseAll();
		}
	}

	private void start(Step step) {
		busy = true;
		cancelled.set(false);
		listener.loadingStarted();
		new LoadWorker(step).execute();
	}

	private void releaseAll() {
		pendingResultSet.ifPresent(ResultSetLoader::closeQuietly);
		pendingResultSet = Optional.empty();
		statement.ifPresent(ResultSetLoader::closeQuietly);
		statement = Optional.empty();
	}

	private static void closeQuietly(AutoCloseable closeable) {
		try {
			closeable.close();
		} catch (Exception e) {
			System.err.println(e + " while closing " + closeable);
		}
	}

	private final class LoadWorker extends SwingWorker<Void, Void> implements ExecutionFeedback {

		private final Step step;
		private final ResultSetRenderer renderer = ResultSetLoader.this.renderer;

		LoadWorker(Step step) {
			this.step = step;
		}

		@Override
		protected Void doInBackground() {
			try {
				step.run(this);
				// Move on to the statement's next result once the current one has been fully read
				while (pendingResultSet.isEmpty() && statement.isPresent()) {
					ExecutionResult result = DisplayResultSetUtil.getResult(Optional.empty(), statement,
							Optional.empty(), connection, this);
					statement = result.statement();
					if (result.resultSet().isPresent()) {
						fetchFirstBatch(result.resultSet().get());
					}
				}
			} catch (ThreadKilledException | SQLException | SQLMinusException e) {
				showMessage(e.getMessage());
				releaseAll();
			} catch (Exception e) {
				e.printStackTrace();
				showMessage(e.toString());
				releaseAll();
			}
			return null;
		}

		@Override
		protected void done() {
			try {
				get();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (ExecutionException e) {
				// An Error such as OutOfMemoryError escaped doInBackground
				e.getCause().printStackTrace();
				renderer.showMessage(e.getCause().toString());
				releaseAll();
			}
			busy = false;
			if (pendingResultSet.isPresent()) {
				listener.moreRowsAvailable(lastBatchSize, totalRows);
			} else {
				listener.loadingFinished();
			}
		}

		void fetchFirstBatch(ResultSet rst) throws SQLException, ThreadKilledException {
			try {
				ResultSetMetaData metaData = rst.getMetaData();
				int columnCount = metaData.getColumnCount();
				columnLabels = new ArrayList<>(columnCount);
				columnTypes = new int[columnCount];
				for (int i = 1; i <= columnCount; i++) {
					columnLabels.add(metaData.getColumnLabel(i));
					columnTypes[i - 1] = metaData.getColumnType(i);
				}
				totalRows = 0;
			} catch (SQLException | RuntimeException e) {
				closeQuietly(rst);
				throw e;
			}
			fetchBatch(rst, rowLimit, true);
		}

		/**
		 * Reads up to {@code limit} rows. Between batches the cursor of a pending result set is left on the first
		 * row not yet read.
		 */
		void fetchBatch(ResultSet rst, Optional<Integer> limit, boolean firstBatch)
				throws SQLException, ThreadKilledException {
			try {
				boolean hasAnotherRow = firstBatch ? rst.next() : true;
				List<List<Object>> rows = new ArrayList<>();
				while (hasAnotherRow && (limit.isEmpty() || rows.size() < limit.get())) {
					if (cancelled.get()) {
						throw new ThreadKilledException("Thread killed");
					}
					List<Object> row = new ArrayList<>(columnTypes.length);
					for (int i = 1; i <= columnTypes.length; i++) {
						row.add(renderer.readValue(rst, i, columnTypes[i - 1]));
					}
					rows.add(row);
					hasAnotherRow = rst.next();
				}

				totalRows += rows.size();
				int total = totalRows;
				SwingUtilities.invokeLater(
						renderer.prepare(new ResultSetRenderer.Batch(columnLabels, columnTypes, rows, firstBatch, total)));
				SwingUtilities.invokeLater(() -> listener.rowsLoaded(total));

				if (limit.isPresent() && limit.get() < 1) {
					showMessage("You have set the number of rows to be fetched to " + limit.get()
							+ "\nNo rows will be displayed");
				}
				if (hasAnotherRow && limit.isPresent() && limit.get() > 0) {
					pendingResultSet = Optional.of(rst);
					lastBatchSize = limit.get();
				} else {
					rst.close();
				}
			} catch (SQLException | ThreadKilledException | RuntimeException e) {
				closeQuietly(rst);
				throw e;
			}
		}

		@Override
		public void showMessage(String message) {
			SwingUtilities.invokeLater(() -> renderer.showMessage(message));
		}

		@Override
		public boolean confirm(String message, String title) {
			AtomicBoolean confirmed = new AtomicBoolean(false);
			try {
				SwingUtilities.invokeAndWait(() -> confirmed.set(JOptionPane.showConfirmDialog(null, message, title,
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (InvocationTargetException e) {
				e.getCause().printStackTrace();
			}
			return confirmed.get();
		}
	}
}
