package org.misc.sqlminus;
/*
 * Portions copyright Sun Microsystems.
 */

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Types;

/*
 * Copyright (c) 2002 Sun Microsystems, Inc. All  Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * -Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduct the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT
 * BE LIABLE FOR ANY DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT
 * OF OR RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THE SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE, EVEN
 * IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that Software is not designed, licensed or intended for
 * use in the design, construction, operation or maintenance of any nuclear
 * facility.
 */

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

public class DisplayResultSetTableModel extends AbstractTableModel {

	private volatile Vector rowData, columnNames;

	// this defines the actual order of the rows,
	// ie row 1 is the row at rowIndexes[1], which may not always be the actual
	// first row
	// if the sorted order is different from the original row order.
	// eg: rowIndexes[1] could be 8 so after sorting row:1 should be row:8 of the
	// original row order
	private volatile int[] rowIndexes;

	// private boolean ascending=false;//determines the sort order.
	private int compares = 0;// just an informational variable to show the number of compares done for a
								// single sorting.
	private int columnCompared = 0;// determines which column is compared to sort the row.
	private String nullRep;
	private volatile int[] columnTypes;

	public DisplayResultSetTableModel() {
		rowData = new Vector();
		columnNames = new Vector();
		rowIndexes = new int[0];
		columnTypes = new int[0];
		nullRep = "<null>";
	}

	public void setNullRep(String nullRep) {
		this.nullRep = nullRep;
	}

	public void setDataVector(Vector rowData, Vector columnNames, int[] columnTypes) {
		if (columnNames.size() != columnTypes.length)
			throw new IllegalArgumentException(
					"The number of column headings does not equal the number of column types");
		this.rowData.removeAllElements();
		this.rowData.addAll(rowData);
		this.columnNames.removeAllElements();
		this.columnNames.addAll(columnNames);
		this.columnTypes = columnTypes;
		reallocateIndexes();
		fireTableChanged(null);
	}

	private void reallocateIndexes() {
		// the order of the rows is made the same as the original order
		rowIndexes = new int[getRowCount()];
		for (int i = 0; i < rowIndexes.length; i++) {
			rowIndexes[i] = i;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	//
	// Implementation of the TableModel Interface
	//
	//////////////////////////////////////////////////////////////////////////

	public String getColumnName(int column) {
		try {
			if (columnNames.elementAt(column) == null)
				return nullRep;
			else
				return (String) columnNames.elementAt(column);
		} catch (ArrayIndexOutOfBoundsException ae) {
			System.err.println(ae + ", no item at " + column + "\n");
			return "";
		}
	}

	public int getColumnCount() {
		return columnNames.size();
	}

	public int getRowCount() {
		return rowData.size();
	}

	public Object getValueAt(int aRow, int aColumn) {
		try {
			// Vector row = (Vector)rowData.elementAt(aRow);
			// The following will simply return the row at "aRow" if the table
			// sorting order has not been changed, else it will return the
			// row that should be at "aRow" position after sorting
			Vector row = (Vector) rowData.elementAt(rowIndexes[aRow]);
			// if( row.elementAt(aColumn)==null && (getColumnClass(aColumn)==String.class ||
			// getColumnClass(aColumn)==Object.class) )
			if (row.elementAt(aColumn) == null && getColumnClass(aColumn) == String.class)
				return nullRep;
			else
				return row.elementAt(aColumn);
		} catch (ArrayIndexOutOfBoundsException ae) {
			System.err.println(ae + ", no item at " + aRow + "x" + aColumn + "\n");
			return "";
		}
	}

	public boolean isCellEditable(int row, int column) {
		return true;
	}

	public static Class getClassForType(int type) {
		switch (type) {
		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			return String.class;

		case Types.BIT:
			return Boolean.class;

		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
			return Integer.class;

		case Types.BIGINT:
			return Long.class;

		case Types.FLOAT:
		case Types.DOUBLE:
		case Types.NUMERIC:
			return Double.class;

		case Types.DATE:
		case Types.TIME:
		case Types.TIMESTAMP:
			return java.util.Date.class;

		default:
			return String.class;
		}
	}

	public Class getColumnClass(int column) {
		try {
			int type = columnTypes[column];
			return getClassForType(type);
		} catch (ArrayIndexOutOfBoundsException ae) {
			System.err.println(ae + ", no item at " + column + "\n");
			return Object.class;
		}
	}

	////////// End of TableModel Interface ///////////////////////////////

	/**
	 * Adds a mouse listener to the table header to detect requests for a change in
	 * the table sort order. <code>table</code> <b>must</b> be the same table as the
	 * one for which this object is set as the table model.
	 */
	public void addMouseListenerToHeaderInTable(JTable table) {
		final JTable tableView = table;
		// tableView.setColumnSelectionAllowed(false);
		MouseAdapter listMouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				TableColumnModel columnModel = tableView.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				int column = tableView.convertColumnIndexToModel(viewColumn);
				if (e.getClickCount() == 1 && column != -1) {
					// int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK;
					// boolean ascending = (shiftPressed == 0);
					sortByColumn(column, (e.getModifiers() & InputEvent.SHIFT_MASK) == 0);
				}
			}
		};
		JTableHeader th = tableView.getTableHeader();
		th.addMouseListener(listMouseListener);
	}

	private void sortByColumn(int column, boolean ascending) {
		System.err.println("Sorting ..." + (ascending ? "ascending" : "descending"));///////////////////
		// this.ascending = ascending;
		if (rowIndexes.length != getRowCount()) {
			System.err.println("Internal inconsistency detected, cannot sort.");
			return;
		}
		columnCompared = column;
		compares = 0;
		shuttlesort((int[]) rowIndexes.clone(), rowIndexes, 0, rowIndexes.length, ascending);
		System.err.println("Compares done: " + compares);
		fireTableChanged(null);
	}

	// This is a home-grown implementation which we have not had time
	// to research - it may perform poorly in some circumstances. It
	// requires twice the space of an in-place algorithm and makes
	// NlogN assigments shuttling the values between the two
	// arrays. The number of compares appears to vary between N-1 and
	// NlogN depending on the initial order but the main reason for
	// using it here is that, unlike qsort, it is stable.
	private void shuttlesort(int from[], int to[], int low, int high, boolean ascending) {
		if (high - low < 2) {
			return;
		}
		int middle = (low + high) / 2;
		shuttlesort(to, from, low, middle, ascending);
		shuttlesort(to, from, middle, high, ascending);

		int p = low;
		int q = middle;

		/*
		 * This is an optional short-cut; at each recursive call, check to see if the
		 * elements in this subset are already ordered. If so, no further comparisons
		 * are needed; the sub-array can just be copied. The array must be copied rather
		 * than assigned otherwise sister calls in the recursion might get out of sinc.
		 * When the number of elements is three they are partitioned so that the first
		 * set, [low, mid), has one element and and the second, [mid, high), has two. We
		 * skip the optimisation when the number of elements is three or less as the
		 * first compare in the normal merge will produce the same sequence of steps.
		 * This optimisation seems to be worthwhile for partially ordered lists but some
		 * analysis is needed to find out how the performance drops to Nlog(N) as the
		 * initial order diminishes - it may drop very quickly.
		 */

		if (high - low >= 4 && compare(from[middle - 1], from[middle], ascending) <= 0) {
			for (int i = low; i < high; i++) {
				to[i] = from[i];
			}
			return;
		}

		// A normal merge.

		for (int i = low; i < high; i++) {
			if (q >= high || (p < middle && compare(from[p], from[q], ascending) <= 0)) {
				to[i] = from[p++];
			} else {
				to[i] = from[q++];
			}
		}
	}

	private int compare(int row1, int row2, boolean ascending) {
		compares++;
		int result = compareRowsByColumn(row1, row2, columnCompared);
		if (result != 0) {
			return ascending ? result : -result;
		}
		return 0;
	}

	private int compareRowsByColumn(int row1, int row2, int column) {
		Class type = getColumnClass(column);
		// Check for nulls

		Object o1 = ((Vector) rowData.elementAt(row1)).elementAt(column);
		Object o2 = ((Vector) rowData.elementAt(row2)).elementAt(column);

		// If both values are null return 0
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 == null) { // Define null less than everything.
			return -1;
		} else if (o2 == null) {
			return 1;
		}
		/*
		 * We copy all returned values from the getValue call in case an optimised model
		 * is reusing one object to return many values. The Number subclasses in the JDK
		 * are immutable and so will not be used in this way but other subclasses of
		 * Number might want to do this to save space and avoid unnecessary heap
		 * allocation.
		 */
		if (type.getSuperclass() == java.lang.Number.class) {
			Number n1 = (Number) o1;
			double d1 = n1.doubleValue();
			Number n2 = (Number) o2;
			double d2 = n2.doubleValue();

			if (d1 < d2)
				return -1;
			else if (d1 > d2)
				return 1;
			else
				return 0;
		} else if (type == java.util.Date.class) {
			// }else if(type == java.sql.Date.class){
			java.util.Date d1 = (java.util.Date) o1;
			// long n1 = d1.getTime();
			java.util.Date d2 = (java.util.Date) o2;
			// long n2 = d2.getTime();

			int comparison = d1.compareTo(d2);

			/*
			 * if (n1 < n2) return -1; else if (n1 > n2) return 1; else return 0;
			 */

			if (comparison < 0)
				return -1;
			else if (comparison > 0)
				return 1;
			else
				return 0;
		} else if (type == String.class) {
			String s1 = (String) o1;
			String s2 = (String) o2;
			// int result = s1.compareTo(s2);
			java.text.Collator coll = java.text.Collator.getInstance();
			coll.setStrength(coll.PRIMARY);
			int result = coll.compare(s1, s2);

			if (result < 0)
				return -1;
			else if (result > 0)
				return 1;
			else
				return 0;
		} else if (type == Boolean.class) {
			Boolean bool1 = (Boolean) o1;
			boolean b1 = bool1.booleanValue();
			Boolean bool2 = (Boolean) o2;
			boolean b2 = bool2.booleanValue();

			if (b1 == b2)
				return 0;
			else if (b1) // Define false < true
				return 1;
			else
				return -1;
		} else {
			Object v1 = o1;
			String s1 = v1.toString();
			Object v2 = o2;
			String s2 = v2.toString();
			// int result = s1.compareTo(s2);
			java.text.Collator coll = java.text.Collator.getInstance();
			coll.setStrength(coll.PRIMARY);
			int result = coll.compare(s1, s2);

			if (result < 0)
				return -1;
			else if (result > 0)
				return 1;
			else
				return 0;
		}
	}

}
