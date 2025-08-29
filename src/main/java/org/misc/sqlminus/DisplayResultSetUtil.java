package org.misc.sqlminus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class DisplayResultSetUtil {

	public static ResultSet getMetadataResult(MetadataRequestEntity metadataRequestEntity,
			Optional<Connection> connection) throws SQLException, SQLMinusException {
		if (connection.isEmpty()) {
			throw new SQLMinusException("connection not available");
		}
		ResultSet rst;
		switch (metadataRequestEntity.metadataRequestType()) {
		case CATALOGS:
			rst = connection.get().getMetaData().getCatalogs();
			break;
		case SCHEMAS:
			rst = connection.get().getMetaData().getSchemas(metadataRequestEntity.catalogName(),
					metadataRequestEntity.schemaPattern());
			break;
		case TABLES:
			rst = connection.get().getMetaData().getTables(metadataRequestEntity.catalogName(),
					metadataRequestEntity.schemaPattern(), metadataRequestEntity.tablePattern(), null);
			break;
		case COLUMNS:
			rst = connection.get().getMetaData().getColumns(metadataRequestEntity.catalogName(),
					metadataRequestEntity.schemaPattern(), metadataRequestEntity.tablePattern(),
					metadataRequestEntity.columnPattern());
			break;
		default:
			throw new SQLMinusException("Unknown metadata request type " + metadataRequestEntity.metadataRequestType());
		}

		return rst;
	}
}
