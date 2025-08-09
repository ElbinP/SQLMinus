package org.misc.sqlminus;

public record MetadataRequestEntity(String catalogName, String schemaPattern, String tablePattern, String columnPattern,
		MetadataRequestType metadataRequestType) {
	public enum MetadataRequestType {
		CATALOGS, SCHEMAS, TABLES, COLUMNS;
	}
}
