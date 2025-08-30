package org.misc.sqlminus;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.misc.sqlminus.StatementFactory.StatementWithOutParams;

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

	private static void showUpdateMessage(Optional<JTextArea> textOutput, boolean showPopupMessage, int updateCount) {
		if (showPopupMessage) {
			JOptionPane.showMessageDialog(null, updateCount + " row(s) updated");
		}
		if (textOutput.isPresent()) {
			textOutput.get().append("\n\n" + updateCount + " row(s) updated");
		}
	}

	private static void showOutparamValues(Optional<JTextArea> textOutput, boolean showPopupMessage,
			Optional<String> mainError, Optional<List<CallableHelper.OutParam>> outParams, CallableStatement cs)
			throws SQLException {
		Map<String, Object> results;

		if (outParams.isPresent()) {
			results = CallableHelper.readOutParams(cs, outParams.get());
		} else {
			results = new LinkedHashMap<>();
		}

		String formatted = formatOutParams(mainError, results);

		if (showPopupMessage) {
			JOptionPane.showMessageDialog(null, formatted);
		}
		textOutput.ifPresent(area -> area.append("\n\n" + formatted));
	}

	private static String formatOutParams(Optional<String> mainError, Map<String, Object> results) {
		StringBuilder sb = new StringBuilder();

		// Append error if present
		mainError.ifPresent(err -> sb.append("Error: ").append(err).append("\n"));

		// Append OUT param values if available
		if (!results.isEmpty()) {
			results.forEach(
					(k, v) -> sb.append(k).append(" = ").append(v == null ? "NULL" : v.toString()).append("\n"));
		}

		return sb.toString();
	}

	public static ExecutionResult getResult(Optional<String> executionCommand, Optional<Statement> statement,
			Optional<MetadataRequestEntity> metadataRequestEntity, Optional<Connection> connection,
			SQLMinus sqlMinusObject, Optional<JTextArea> textOutput, boolean showPopupMessage)
			throws SQLMinusException, SQLException {

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
				StatementWithOutParams statementWithOutParams = StatementFactory.getStatement(connection.get(),
						executionCommand.get());
				stmtInternal = Optional.of(statementWithOutParams.statement());

				boolean executeStatus = false;
				if (stmtInternal.get() instanceof CallableStatement callableStatement) {
					Optional<String> mainError = Optional.empty();
					try {
						executeStatus = callableStatement.execute();
					} catch (SQLException se) {
						mainError = Optional.of(se.getMessage());
					}
					showOutparamValues(textOutput, showPopupMessage, mainError, statementWithOutParams.outParams(),
							callableStatement);

				} else {
					executeStatus = stmtInternal.get().execute(executionCommand.get());
				}

				int updateCount;
				if (executeStatus) {
					rstOptional = Optional.of(stmtInternal.get().getResultSet());
				} else if ((updateCount = stmtInternal.get().getUpdateCount()) != -1) {
					showUpdateMessage(textOutput, showPopupMessage, updateCount);
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
				showUpdateMessage(textOutput, showPopupMessage, updateCount);
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
