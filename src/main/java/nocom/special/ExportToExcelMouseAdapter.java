package nocom.special;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

/**
 * A mouse adapter for JTable that shows a popup menu with Export to Excel menu
 * item.
 */
public class ExportToExcelMouseAdapter extends MouseAdapter implements ActionListener {

	private JPopupMenu popup;
	private JMenuItem exportToExcel;
	private JTable table;
	private final String EXPORT_TO_EXCEL_MENU_TEXT = "Export to Excel Workbook";
	private ExportToExcelHelper excelHelper;

	public ExportToExcelMouseAdapter() {
		super();

		exportToExcel = new JMenuItem(EXPORT_TO_EXCEL_MENU_TEXT);
		exportToExcel.addActionListener(this);
		popup = new JPopupMenu();
		popup.add(exportToExcel);

		excelHelper = new ExportToExcelHelper();
	}

	public JFileChooser getFileChooser() {
		return excelHelper.getFileChooser();
	}

	public JPopupMenu getPopupMenu() {
		return popup;
	}

	public void actionPerformed(ActionEvent ae) {
		if (table != null) {
			String command = ae.getActionCommand();
			if (command.equals(EXPORT_TO_EXCEL_MENU_TEXT)) {
				excelHelper.triggerExportToXlsx(table);
			}

			table = null;
		}
	}

	public void mouseReleased(MouseEvent me) {
		Component eventSource = me.getComponent();
		if (eventSource instanceof JTable) {
			table = (JTable) eventSource;
			if (me.getModifiers() == me.BUTTON3_MASK && table.isEnabled()) {
				popup.show(me.getComponent(), me.getX(), me.getY());
			}
		}
	}
}
