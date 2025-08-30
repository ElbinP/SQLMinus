package org.misc.sqlminus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JOptionPane;

public class DisplayResultSetUtil {

	public static record ExecutionResult(Optional<Statement> statement, Optional<ResultSet> resultSet) {

	}

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

	public static ExecutionResult getResult(Optional<String> executionCommand, Optional<Statement> statement,
			Optional<MetadataRequestEntity> metadataRequestEntity, Optional<Connection> connection,
			SQLMinus sqlMinusObject) throws SQLMinusException, SQLException {

		Optional<Statement> stmtInternal = Optional.empty();
		Optional<ResultSet> rstOptional = Optional.empty();

		List<String> presentParams = new ArrayList<>();

		if (executionCommand.isPresent())
			presentParams.add("execution command");
		if (statement.isPresent())
			presentParams.add("statement");
		if (metadataRequestEntity.isPresent())
			presentParams.add("metadata request entity");

		if (presentParams.size() != 1) {
			throw new SQLMinusException(
					"Exactly one parameter must be provided, but found: " + String.join(", ", presentParams));
		} else if (executionCommand.isPresent()) {
			if (connection.isPresent()) {
				stmtInternal = Optional.of(StatementFactory.getStatement(connection.get(), executionCommand.get()));
				int updateCount;
				if (stmtInternal.get().execute(executionCommand.get())) {
					rstOptional = Optional.of(stmtInternal.get().getResultSet());
				} else if ((updateCount = stmtInternal.get().getUpdateCount()) != -1) {
					sqlMinusObject.popMessage(updateCount + " row(s) updated");
				}
			} else {
				throw new SQLMinusException("Not connected");
			}
		} else if (statement.isPresent()) {
			stmtInternal = statement;
			int updateCount;
			if (stmtInternal.get().getMoreResults()) {
				if (JOptionPane.showConfirmDialog(null, "Show the next resultset?", "Next?",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					rstOptional = Optional.of(stmtInternal.get().getResultSet());
				}
			} else if ((updateCount = stmtInternal.get().getUpdateCount()) != -1) {
				sqlMinusObject.popMessage(updateCount + " row(s) updated");
			} else {
				stmtInternal.get().close();
				stmtInternal = Optional.empty();
			}
		} else if (metadataRequestEntity.isPresent()) {
			rstOptional = Optional.of(DisplayResultSetUtil.getMetadataResult(metadataRequestEntity.get(), connection));
		} else {
			throw new SQLMinusException("None of execution command, result set and metadata request provided");
		}

		return new ExecutionResult(stmtInternal, rstOptional);
	}
}
