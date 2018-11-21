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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.misc.sqlminus.sqlhistory.entity.SQLHistory;

public class SQLHistoryHelper {

	public static List<String> getSQLCommandsFromHistory() throws IOException, JAXBException {
		String homeDirectory = System.getProperty("user.home");
		File sqlHistoryZipFile = new File(homeDirectory + "/.org.misc.sqlminus/SQLHistory.xml.gz");
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
		String homeDirectory = System.getProperty("user.home");
		File sqhistoryZipFile = new File(homeDirectory + "/.org.misc.sqlminus/SQLHistory.xml.gz");
		File parent = sqhistoryZipFile.getParentFile();
		if (!parent.exists() && !parent.mkdirs()) {
			throw new IllegalStateException("Couldn't create dir: " + parent);
		}
		if (!sqhistoryZipFile.exists() || (sqhistoryZipFile.exists() && !sqhistoryZipFile.isDirectory())) {
			JAXBContext jaxbContext = JAXBContext.newInstance(SQLHistory.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			SQLHistory sqlHistory = new SQLHistory();
			sqlHistory.setSqlCommands(sqlCommands);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			FileOutputStream fos = new FileOutputStream(sqhistoryZipFile, false);
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
					"Unable to create file " + sqhistoryZipFile.getPath() + ". Folder exists with same name");
		}
	}

}
