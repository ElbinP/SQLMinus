package org.misc.sqlminus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

/**
 * Renders result sets in a {@link SortableTable}.
 */
public class GridResultRenderer implements ResultSetRenderer {

	private final SortableTable table;
	private final String nullRep;

	public GridResultRenderer(SortableTable table, String nullRep) {
		this.table = table;
		this.nullRep = nullRep;
	}

	@Override
	public Object readValue(ResultSet rst, int column, int columnType) throws SQLException {
		if (DisplayResultSetTableModel.getClassForType(columnType) == String.class) {
			return rst.getString(column);
		}
		return rst.getObject(column);
	}

	@Override
	public Runnable prepare(Batch batch) {
		Vector<Vector<Object>> rows = new Vector<>(batch.rows().size());
		for (List<Object> row : batch.rows()) {
			rows.add(new Vector<>(row));
		}
		int[] columnTypes = batch.columnTypes();

		if (batch.firstBatch()) {
			Vector<String> columnHeadings = new Vector<>(batch.columnLabels());
			return () -> {
				table.setNullRep(nullRep);
				table.changeTable(rows, columnHeadings, columnTypes);
				table.repaint();
			};
		}
		return () -> {
			table.appendTable(rows, columnTypes);
			int lastRow = table.getRowCount() - 1;
			if (lastRow >= 0) {
				table.scrollRectToVisible(table.getCellRect(lastRow, 0, true));
			}
		};
	}

	@Override
	public void showMessage(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
}
