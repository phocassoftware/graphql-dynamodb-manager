package com.phocassoftware.graphql.database.manager.test;

import static com.phocassoftware.graphql.database.manager.test.DynamoDbInitializer.*;

import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phocassoftware.graphql.database.manager.Database;
import com.phocassoftware.graphql.database.manager.VirtualDatabase;
import com.phocassoftware.graphql.database.manager.dynamo.DynamoDbManager;
import com.phocassoftware.graphql.database.manager.test.TestDatabaseProvider.ServerWrapper;
import com.phocassoftware.graphql.database.manager.test.annotations.*;

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class ArgumentProvider {

	private final String uniqueId;
	private final ServerWrapper wrapper;
	private final String organisationId;
	private final Parameter parameter;
	private final Boolean withHistory;
	private final boolean hashed;
	private final String classPath;
	private final ObjectMapper objectMapper;

	public ArgumentProvider(
		String uniqueId,
		ServerWrapper wrapper,
		String organisationId,
		Parameter parameter,
		Boolean withHistory,
		boolean hashed,
		String classPath,
		ObjectMapper objectMapper
	) {
		this.uniqueId = uniqueId;
		var databaseOrganisation = parameter.getAnnotation(DatabaseOrganisation.class);
		organisationId = databaseOrganisation != null ? databaseOrganisation.value() : organisationId;

		this.wrapper = wrapper;
		this.organisationId = organisationId;
		this.parameter = parameter;
		this.withHistory = withHistory;
		this.hashed = hashed;
		this.classPath = classPath;
		this.objectMapper = objectMapper;

	}

	public DynamoDbManager getDynamoDbManager() {
		try {
			var client = wrapper.clientAsync();
			final var databaseNames = parameter.getAnnotation(DatabaseNames.class);
			String[] tables;
			String historyTable = null;
			if (databaseNames != null) {
				tables = Stream.of(databaseNames.value()).map(name -> name + "_" + uniqueId).toArray(String[]::new);
				for (final String table : tables) {
					createTable(client, table);
					if (withHistory) {
						historyTable = table + "_history";
						createHistoryTable(client, historyTable);
					}
				}
			} else {
				tables = new String[] { "table" };
				historyTable = "table_history";
			}

			final var globalEnabledAnnotation = parameter.getAnnotation(GlobalEnabled.class);

			var globalEnabled = true;
			if (globalEnabledAnnotation != null) {
				globalEnabled = globalEnabledAnnotation.value();
			}

			return getDatabaseManager(client, tables, historyTable, globalEnabled, hashed, classPath, "parallelIndex", objectMapper);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public HistoryProcessor getHistoryProcessor() {
		return new HistoryProcessor(wrapper.client(), wrapper.streamClient(), parameter, organisationId);
	}

	public DynamoDbAsyncClient getClientAsync() {
		return wrapper.clientAsync();
	}

	public DynamoDbClient getClient() {
		return wrapper.client();
	}

	public Database getDatabase() {
		return getEmbeddedDatabase(getDynamoDbManager(), organisationId);
	}

	public VirtualDatabase getVirtualDatabase() {
		return new VirtualDatabase(getDatabase());
	}

}
