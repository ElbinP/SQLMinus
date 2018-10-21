package nocom.special;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextArea;

public class MultilineTextCellEditor extends DefaultCellEditor {

	private JTextArea textArea;

	public MultilineTextCellEditor(JTextArea textArea) {
		super(new JCheckBox());
		this.textArea = textArea;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		textArea.setText(value.toString());

		return textArea;
	}

	public Object getCellEditorValue() {
		return textArea.getText();
	}

}
