package nocom.special;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExportToExcelHelper {

	private JFileChooser fileChooser;
	private FileSaverThread fileSaver;

	public ExportToExcelHelper() {
		fileChooser = new JFileChooser(System.getProperty("user.home"));
		fileSaver = new FileSaverThread();
	}

	public JFileChooser getFileChooser() {
		return fileChooser;
	}

	public void triggerExportToXlsx(JTable table) {
		int returnValue = fileChooser.showSaveDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String s = fileChooser.getSelectedFile().getPath();
			if (FilenameUtils.getExtension(s).length() == 0) {
				s = s + ".xlsx";
			}
			File f = new File(s);
			if (f.exists()) {
				int overwrite = JOptionPane.showConfirmDialog(null, "Do you want to overwrite the file", "Overwrite?",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (overwrite == JOptionPane.YES_OPTION) {
					startExcelSaveThread(fileSaver.setFile(f, table));
				}
			} else {
				startExcelSaveThread(fileSaver.setFile(f, table));
			}
		}
	}

	private void exportToXlsx(JTable table, File saveToFile) throws IOException {

		XSSFWorkbook wb = new XSSFWorkbook(); // Excell workbook
		Sheet sheet = wb.createSheet(); // WorkSheet
		TableModel model = table.getModel(); // Table model

		Row headerRow = sheet.createRow(0); // Create row at line 0
		XSSFFont boldFont = wb.createFont();
		boldFont.setBold(true);
		CellStyle headerRowStyle = wb.createCellStyle();
		headerRowStyle.setFont(boldFont);

		Cell headerCell;
		for (int headings = 0; headings < model.getColumnCount(); headings++) { // For each column
			headerCell = headerRow.createCell(headings);
			headerCell.setCellValue(model.getColumnName(headings));// Write column name
			headerCell.setCellStyle(headerRowStyle);
		}

		Row row;
		Cell cell;
		CellStyle cellStyle = wb.createCellStyle();
		cellStyle.setWrapText(true);
		for (int rows = 0; rows < model.getRowCount(); rows++) { // For each table row
			// Set the row to the next one in the sequence
			row = sheet.createRow((rows + 1));
			for (int cols = 0; cols < model.getColumnCount(); cols++) { // For each table column
				Object cellValue = model.getValueAt(rows, cols);
				cell = row.createCell(cols);
				if (cellValue != null) {
					cell.setCellValue(cellValue.toString()); // Write value
				} else {
					cell.setCellValue("");
				}
				cell.setCellStyle(cellStyle);
			}
			row.setHeight((short) -1);
		}

		for (int headings = 0; headings < model.getColumnCount(); headings++) { // For each column
			sheet.autoSizeColumn(headings);
		}

		FileOutputStream outputStream = new FileOutputStream(saveToFile.getPath());
		try {
			wb.write(outputStream);// Save the file
		} finally {
			outputStream.close();
			wb.close();
		}
	}

	private void startExcelSaveThread(Runnable runnable) {
		Thread excelSaveThread = new Thread(runnable, "EXCEL_EXPORT_THREAD");
		excelSaveThread.start();
	}

	private class FileSaverThread implements java.lang.Runnable {
		private File f;
		private JTable table;

		public FileSaverThread setFile(File f, JTable table) {
			this.f = f;
			this.table = table;
			return this;
		}

		public void run() {
			if (f != null) {
				try {
					exportToXlsx(table, f);
					JOptionPane.showMessageDialog(null, "Export to file complete", "Export to Excel",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error saving results to file. " + e, "Export to Excel",
							JOptionPane.ERROR_MESSAGE);
				} finally {
					f = null;
				}
			}
		}
	}

}
