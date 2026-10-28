package org.misc.sqlminus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Presents the result sets fetched by {@link ResultSetLoader}. {@link #readValue} and {@link #prepare} are called on
 * the loader's worker thread and must not touch Swing components; {@link #showMessage} and the {@link Runnable}
 * returned by {@link #prepare} are run on the event dispatch thread.
 */
public interface ResultSetRenderer {

	/**
	 * A batch of rows read from one result set. {@code firstBatch} is set for the first batch of each result set;
	 * {@code totalRows} includes the rows of this batch.
	 */
	record Batch(List<String> columnLabels, int[] columnTypes, List<List<Object>> rows, boolean firstBatch,
			int totalRows) {
	}

	Object readValue(ResultSet rst, int column, int columnType) throws SQLException;

	/**
	 * Does any expensive work needed to display the batch and returns the Swing update that displays it.
	 */
	Runnable prepare(Batch batch);

	void showMessage(String message);
}
