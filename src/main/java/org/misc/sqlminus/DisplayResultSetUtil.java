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

import org.misc.sqlminus.StatementFactory.StatementWithOutParams;

public class DisplayResultSetUtil {

	public static record ExecutionResult(Optional<Statement> statement, Optional<ResultSet> resultSet) {

	}

	/**
	 * Lets the caller decide how messages and questions raised while executing are presented.
	 */
	public interface ExecutionFeedback {
		void showMessage(String message);

		boolean confirm(String message, String title);
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

	private static void showOutparamValues(ExecutionFeedback feedback, Optional<String> mainError,
			Optional<List<CallableHelper.OutParam>> outParams, CallableStatement cs)
			throws SQLException {
		Map<String, Object> results;

		if (outParams.isPresent()) {
			results = CallableHelper.readOutParams(cs, outParams.get());
		} else {
			results = new LinkedHashMap<>();
		}

		feedback.showMessage(formatOutParams(mainError, results));
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
			ExecutionFeedback feedback) throws SQLMinusException, SQLException {

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
					showOutparamValues(feedback, mainError, statementWithOutParams.outParams(), callableStatement);

				} else {
					executeStatus = stmtInternal.get().execute(executionCommand.get());
				}

				int updateCount;
				if (executeStatus) {
					rstOptional = Optional.of(stmtInternal.get().getResultSet());
				} else if ((updateCount = stmtInternal.get().getUpdateCount()) != -1) {
					feedback.showMessage(updateCount + " row(s) updated");
				}

			} else {
				throw new SQLMinusException("Not connected");
			}
		} else if (statement.isPresent()) {
			stmtInternal = statement;
			int updateCount;
			if (stmtInternal.get().getMoreResults()) {
				if (feedback.confirm("Show the next resultset?", "Next?")) {
					rstOptional = Optional.of(stmtInternal.get().getResultSet());
				}
			} else if ((updateCount = stmtInternal.get().getUpdateCount()) != -1) {
				feedback.showMessage(updateCount + " row(s) updated");
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
