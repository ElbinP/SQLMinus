package org.misc.sqlminus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JTextArea;

import nocom.special.UtilityFunctions;

/**
 * Renders result sets as fixed-width text columns. Values longer than the maximum column width are wrapped onto
 * further lines.
 */
public class TextResultRenderer implements ResultSetRenderer {

	private final JTextArea textOutput;
	private final int maxColWidth;
	private final int spacing;
	private final boolean rowDividers;
	private final int maxDataLength;
	private final String nullRep;
	private final boolean clearScreen;

	// Column layout of the current result set, kept so that later batches line up with the first one
	private List<Vector<String>> columnNames;
	private int[] columnSize;

	public TextResultRenderer(JTextArea textOutput, int maxColWidth, int spacing, boolean rowDividers,
			int maxDataLength, String nullRep, boolean clearScreen) {
		this.textOutput = textOutput;
		this.maxColWidth = maxColWidth;
		this.spacing = spacing;
		this.rowDividers = rowDividers;
		this.maxDataLength = maxDataLength;
		this.nullRep = nullRep;
		this.clearScreen = clearScreen;
	}

	@Override
	public Object readValue(ResultSet rst, int column, int columnType) throws SQLException {
		return rst.getString(column);
	}

	@Override
	public Runnable prepare(Batch batch) {
		String text = format(batch);
		if (batch.firstBatch()) {
			return () -> {
				if (clearScreen) {
					textOutput.setText("");
				}
				textOutput.append(text);
			};
		}
		return () -> {
			textOutput.append(text);
			textOutput.setCaretPosition(textOutput.getDocument().getLength());
		};
	}

	@Override
	public void showMessage(String message) {
		textOutput.append("\n\n" + message + "\n");
	}

	String format(Batch batch) {
		int columnCount = batch.columnLabels().size();
		if (batch.firstBatch()) {
			columnNames = new ArrayList<>(columnCount);
			columnSize = new int[columnCount];
			for (int i = 0; i < columnCount; i++) {
				Vector<String> name = split(batch.columnLabels().get(i));
				columnNames.add(name);
				columnSize[i] = maxLength(name);
			}
		}

		List<List<Vector<String>>> cells = new ArrayList<>(batch.rows().size());
		int maxRowLines = 1;
		for (List<Object> row : batch.rows()) {
			List<Vector<String>> rowCells = new ArrayList<>(columnCount);
			for (int i = 0; i < columnCount; i++) {
				Vector<String> cell = split((String) row.get(i));
				rowCells.add(cell);
				columnSize[i] = Math.max(columnSize[i], maxLength(cell));
				maxRowLines = Math.max(maxRowLines, cell.size());
			}
			cells.add(rowCells);
		}

		int maxRowSize = 0;
		for (int size : columnSize) {
			maxRowSize += size + spacing;
		}

		StringBuilder out = new StringBuilder();
		if (batch.firstBatch()) {
			// The column headings are always displayed even when there are no rows to display
			int headerLines = columnNames.stream().mapToInt(Vector::size).max().orElse(1);
			appendLines(out, columnNames, headerLines);
			out.append('\n');
			for (int i = 0; i < columnCount; i++) {
				int nameLength = maxLength(columnNames.get(i));
				out.append("_".repeat(nameLength)).append(" ".repeat(columnSize[i] - nameLength + spacing));
			}
		}
		for (List<Vector<String>> rowCells : cells) {
			appendLines(out, rowCells, rowCells.stream().mapToInt(Vector::size).max().orElse(1));
			if (rowDividers || maxRowLines > 1) {
				out.append('\n').append("-".repeat(maxRowSize));
			}
		}

		if (batch.firstBatch()) {
			out.append("\n\n").append(batch.rows().size()).append(" row(s) selected\n");
		} else {
			out.append("\n\n").append(batch.totalRows()).append(" row(s) selected (total)\n");
		}
		return out.toString();
	}

	private void appendLines(StringBuilder out, List<Vector<String>> columns, int lineCount) {
		for (int m = 0; m < lineCount; m++) {
			out.append('\n');
			for (int i = 0; i < columns.size(); i++) {
				Vector<String> column = columns.get(i);
				String line = m < column.size() ? column.get(m) : "";
				out.append(line).append(" ".repeat(columnSize[i] - line.length() + spacing));
			}
		}
	}

	private Vector<String> split(String value) {
		if (value == null) {
			value = nullRep;
		}
		value = UtilityFunctions.truncateString(value, maxDataLength).replace('\n', ' ').replace('\t', ' ')
				.replace('\r', ' ');
		return UtilityFunctions.splitUpString(value, maxColWidth);
	}

	private static int maxLength(Vector<String> lines) {
		return UtilityFunctions.getMaxLength(lines.toArray(new String[0]));
	}
}
