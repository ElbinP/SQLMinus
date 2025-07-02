package org.misc.sqlminus.sqlhistory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.misc.sqlminus.Constants;
import org.misc.sqlminus.sqlhistory.entity.SQLHistory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

public class SQLHistoryHelper {

	public static List<String> getSQLCommandsFromHistory() throws IOException, JAXBException {
		File sqlHistoryZipFile = new File(Constants.SQL_HISTORY_FULL_FILE_PATH);
		List<String> history = new ArrayList<>();
		if (sqlHistoryZipFile.exists() && !sqlHistoryZipFile.isDirectory()) {
			FileInputStream fis = new FileInputStream(sqlHistoryZipFile);
			GZIPInputStream gis = new GZIPInputStream(fis);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				byte[] buffer = new byte[1024];
				int len;
				while ((len = gis.read(buffer)) != -1) {
					baos.write(buffer, 0, len);
				}
				JAXBContext jaxbContext = JAXBContext.newInstance(SQLHistory.class);

				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				SQLHistory sqlHistory = (SQLHistory) jaxbUnmarshaller
						.unmarshal(new ByteArrayInputStream(baos.toByteArray()));
				if (sqlHistory.getSqlCommands() != null) {
					history.addAll(sqlHistory.getSqlCommands());
				}
			} finally {
				// close resources
				baos.close();
				gis.close();
				fis.close();
			}
		}

		return history;
	}

	public static void saveSQLCommandsToHistory(List<String> sqlCommands) throws JAXBException, IOException {
		sqlCommands.removeIf(s -> s.trim().length() == 0);
		File sqlHistoryZipFile = new File(Constants.SQL_HISTORY_FULL_FILE_PATH);
		File parent = sqlHistoryZipFile.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new IllegalStateException("Couldn't create dir: " + parent);
		}
		if (!sqlHistoryZipFile.exists() || (sqlHistoryZipFile.exists() && !sqlHistoryZipFile.isDirectory())) {
			JAXBContext jaxbContext = JAXBContext.newInstance(SQLHistory.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			SQLHistory sqlHistory = new SQLHistory();
			sqlHistory.setSqlCommands(sqlCommands);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileOutputStream fos = new FileOutputStream(sqlHistoryZipFile, false);
			GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
			try {
				jaxbMarshaller.marshal(sqlHistory, baos);
				gzipOS.write(baos.toByteArray(), 0, baos.size());
			} finally {
				// close resources
				gzipOS.close();
				fos.close();
				baos.close();
			}
		} else {
			throw new IllegalStateException(
					"Unable to create file " + sqlHistoryZipFile.getPath() + ". Folder exists with same name");
		}
	}

}
