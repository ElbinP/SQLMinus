package nocom.special;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTextField;

public class MultilineTextCellEditor extends DefaultCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6202352238862100199L;
	private JTextField textField;
	private String contents;

	public MultilineTextCellEditor(JTextField textField) {
		super(new JCheckBox());
		this.textField = textField;
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		if (value != null) {
			contents = value.toString();
			textField.setText(value.toString());
		} else {
			contents = null;
			textField.setText("");
		}
		return textField;
	}

	public String getMultilineContents() {
		return contents;
	}

}
