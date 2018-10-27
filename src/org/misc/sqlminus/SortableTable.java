package org.misc.sqlminus;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import nocom.special.CustomizedMouseAdapter;
import nocom.special.MultilineTextCellEditor;
import nocom.special.UtilityFunctions;

public class SortableTable extends JTable {

	private MultilineTextCellEditor myCellEditor;
	private JTextField text;
	private CustomizedMouseAdapter textMouseAdapter;
	private DefaultTableCellRenderer cellRenderer;
	private DefaultTableCellRenderer dummyRenderer;
	private int minColWidth, maxColWidth;
	private CellContentsViewFrame contentsViewFrame;
	private Color backgroundColor;
	private Image iconImage;
	private Font tFont;

	public SortableTable(DisplayResultSetTableModel model, int minColWidth, int maxColWidth, Color backgroundColor,
			Image iconImage, Font tFont) {
		super(model);
		this.backgroundColor = backgroundColor;
		this.iconImage = iconImage;
		model.addMouseListenerToHeaderInTable(this);

		text = new JTextField();
		text.setBackground(backgroundColor);
		text.setFont(tFont);
		text.setEditable(false);
		textMouseAdapter = new CustomizedMouseAdapter(text);
		text.addMouseListener(textMouseAdapter);

		myCellEditor = new MultilineTextCellEditor(text);
		cellRenderer = new DefaultTableCellRenderer();
		dummyRenderer = new DefaultTableCellRenderer();
		setMinColWidth(minColWidth);
		setMaxColWidth(maxColWidth);
		contentsViewFrame = new CellContentsViewFrame(backgroundColor);
		contentsViewFrame.getContentPane().setBackground(backgroundColor);
		contentsViewFrame.setIconImage(iconImage);
		contentsViewFrame.setBounds(150, 100, 600, 300);
		contentsViewFrame.setVisible(false);

		text.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				contentsViewFrame.setContent(myCellEditor.getMultilineContents());
				contentsViewFrame.setVisible(true);
			}
		});

		getSelectionModel().addListSelectionListener(e -> {
			contentsViewFrame.setVisible(false);
		});
	}

	public Component getContentsViewFrame() {
		return contentsViewFrame;
	}

	public JPopupMenu getCellEditorPopupMenu() {
		return textMouseAdapter.getPopupMenu();
	}

	public JPopupMenu getContentsViewPopupMenu() {
		return contentsViewFrame.getContentsViewPopupMenu();
	}

	public void changeTable(Vector rowContents, Vector columnHeadings, int[] columnTypes) {
		((DisplayResultSetTableModel) getModel()).setDataVector(rowContents, columnHeadings, columnTypes);
	}

	public void setNullRep(String nullRep) {
		((DisplayResultSetTableModel) getModel()).setNullRep(nullRep);
	}

	public Class getColumnClass(int column) {
		return ((DisplayResultSetTableModel) getModel()).getColumnClass(column);
	}

	private void setCellEditorForAllColumns() {
		for (int i = 0; i < getColumnCount(); i++) {
			getColumnModel().getColumn(i).setCellEditor(myCellEditor);
		}
	}

	public void setCellEditorFont(Font font) {
		text.setFont(font);
		cellRenderer.setFont(font);
		dummyRenderer.setFont(font);
	}

	public void setMinColWidth(int noOfChars) {
		setDummyRendererText(noOfChars);
		minColWidth = dummyRenderer.getPreferredSize().width;
	}

	public void setMaxColWidth(int noOfChars) {
		setDummyRendererText(noOfChars);
		maxColWidth = dummyRenderer.getPreferredSize().width;
	}

	private void setDummyRendererText(int noOfChars) {
		StringBuffer s = new StringBuffer();
		for (int i = 0; i <= noOfChars; i++) {
			s.append(' ');
		}
		dummyRenderer.setText(s.toString());
	}

	public void tableChanged(TableModelEvent e) {
		super.tableChanged(e);
		TableColumn column;
		for (int i = 0; i < getColumnCount(); i++) {
			column = getColumnModel().getColumn(i);
			column.sizeWidthToFit();
			if (column.getWidth() > maxColWidth)
				column.setPreferredWidth(maxColWidth);
		}

		Vector columnContents = new Vector();
		for (int i = 0; i < getColumnCount(); i++) {
			Vector columnVect = new Vector();
			for (int j = 0; j < getRowCount(); j++) {
				columnVect.add(getValueAt(j, i));
			}
			columnContents.add(columnVect);
		}

		int cellWidth;
		String maxLengthString;
		Component comp;
		Vector tempVect;
		for (int i = 0; i < getColumnCount(); i++) {
			tempVect = (Vector) columnContents.elementAt(i);
			if (getColumnClass(i) == java.lang.String.class) {
				maxLengthString = UtilityFunctions
						.getMaxLengthString((String[]) tempVect.toArray(new String[tempVect.size()])) + " ";
			} else if (getColumnClass(i).getSuperclass() == java.lang.Number.class
					|| getColumnClass(i) == java.lang.Object.class) {
				maxLengthString = UtilityFunctions
						.getMaxLengthString((Object[]) tempVect.toArray(new Object[tempVect.size()])) + " ";
			} else if (getColumnClass(i) == java.sql.Date.class) {
				// The following is useless since the table is using some other format to
				// display a Date object.
				maxLengthString = UtilityFunctions
						.getMaxLengthString((Object[]) tempVect.toArray(new Object[tempVect.size()])) + " ";
				// System.err.println(i+","+getDefaultRenderer(java.sql.Date.class).getTableCellRendererComponent(this,
				// tempVect.elementAt(0), false, false, 0, i));
			} else {
				maxLengthString = "";
			}
			try {
				// comp = getDefaultRenderer(String.class).getTableCellRendererComponent(this,
				// maxLengthString, false, false, 0, i);

				cellRenderer.setText(maxLengthString);
				comp = cellRenderer;
				/*
				 * if( getColumnClass(i)==java.sql.Date.class )
				 * comp=getDefaultRenderer(java.sql.Date.class).getTableCellRendererComponent(
				 * this, tempVect.elementAt(3), false, false, 3, i);
				 */

				cellWidth = comp.getPreferredSize().width;
				if (cellWidth > maxColWidth)
					cellWidth = maxColWidth;
				else if ((minColWidth <= maxColWidth) && (cellWidth < minColWidth))
					cellWidth = minColWidth;

				if (cellWidth > getColumnModel().getColumn(i).getWidth())
					getColumnModel().getColumn(i).setPreferredWidth(cellWidth);

			} catch (Exception ex) {
				System.err.println(ex + " while trying to change the column widths");
			}
		}
		setCellEditorForAllColumns();
	}

}
