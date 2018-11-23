package nocom.special;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportToExcelHelper {

	public static void triggerExportToXlsx(JTable table) {
		Thread exportToXlsxThread = new Thread(() -> {
			try {
				ExportToExcelHelper.exportToXlsx(table);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error saving results to file. " + e.getMessage());
			}
		});
		exportToXlsxThread.start();
	}

	private static void exportToXlsx(JTable table) throws IOException {

		// TODO: Give the actual file name here.
		String path = "D:\\export.xlsx";

		Workbook wb = new XSSFWorkbook(); //Excell workbook
		Sheet sheet = wb.createSheet(); //WorkSheet
		Row row = sheet.createRow(2); //Row created at line 3
		TableModel model = table.getModel(); //Table model


		Row headerRow = sheet.createRow(0); //Create row at line 0
		for (int headings = 0; headings < model.getColumnCount(); headings++) { //For each column
			headerRow.createCell(headings).setCellValue(model.getColumnName(headings));//Write column name
		}

		for (int rows = 0; rows < model.getRowCount(); rows++) { //For each table row
			for (int cols = 0; cols < table.getColumnCount(); cols++) { //For each table column
				Object cellValue = model.getValueAt(rows, cols);
				if (cellValue != null) {
					row.createCell(cols).setCellValue(cellValue.toString()); //Write value
				} else {
					row.createCell(cols).setCellValue("");
				}
			}

			//Set the row to the next one in the sequence
			row = sheet.createRow((rows + 3));
		}
		FileOutputStream outputStream = new FileOutputStream(path);
		try {
			wb.write(outputStream);//Save the file
		} finally {
			outputStream.close();
		}
	}

}
