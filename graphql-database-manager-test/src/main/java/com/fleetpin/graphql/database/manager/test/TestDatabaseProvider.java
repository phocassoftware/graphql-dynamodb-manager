/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.fleetpin.graphql.database.manager.test;

import static com.fleetpin.graphql.database.manager.test.DynamoDbInitializer.*;

import com.fleetpin.graphql.database.manager.Database;
import com.fleetpin.graphql.database.manager.dynamo.DynamoDbManager;
import com.fleetpin.graphql.database.manager.test.annotations.DatabaseNames;
import com.fleetpin.graphql.database.manager.test.annotations.DatabaseOrganisation;
import com.fleetpin.graphql.database.manager.test.annotations.GlobalEnabled;
import com.fleetpin.graphql.database.manager.test.annotations.TestDatabase;
import com.fleetpin.graphql.database.manager.util.CompletableFutureUtil;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.streams.DynamoDbStreamsAsyncClient;

public final class TestDatabaseProvider implements ParameterResolver, BeforeEachCallback, AfterEachCallback {

	private static final DbHolder HOLDER = new DbHolder();

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		if (parameterContext.getParameter().getType().isAssignableFrom(DynamoDbManager.class)) {
			return true;
		}
		if (parameterContext.getParameter().getType().isAssignableFrom(HistoryProcessor.class)) {
			return true;
		}
		if (parameterContext.getParameter().getType().isAssignableFrom(Database.class)) {
			return true;
		}
		return false;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
		var store = extensionContext.getStore(Namespace.create(extensionContext.getUniqueId()));
		var arguments = store.get("arguments", List.class);
		return arguments.get(parameterContext.getIndex());
	}

	private Database createDatabase(
		final DynamoDbAsyncClient client,
		final DynamoDbStreamsAsyncClient streamClient,
		final AnnotatedElement parameter,
		final String organisationId,
		final boolean withHistory,
		final boolean hashed,
		final String classPath,
		CompletableFuture<Object> finished
	) throws ExecutionException, InterruptedException {
		final var databaseOrganisation = parameter.getAnnotation(DatabaseOrganisation.class);
		final var correctOrganisationId = databaseOrganisation != null ? databaseOrganisation.value() : organisationId;

		final var dynamoDbManager = createDynamoDbManager(client, streamClient, parameter, withHistory, hashed, classPath);

		return getEmbeddedDatabase(dynamoDbManager, correctOrganisationId, finished);
	}

	private DynamoDbManager createDynamoDbManager(
		final DynamoDbAsyncClient client,
		final DynamoDbStreamsAsyncClient streamClient,
		final AnnotatedElement parameter,
		final boolean withHistory,
		final boolean hashed,
		final String classPath
	) throws ExecutionException, InterruptedException {
		final var databaseNames = parameter.getAnnotation(DatabaseNames.class);
		var tables = databaseNames != null ? databaseNames.value() : new String[] { "table" };

		String historyTable = null;
		for (final String table : tables) {
			createTable(client, table);
			if (withHistory) {
				historyTable = table + "_history";
				createHistoryTable(client, historyTable);
			}
		}

		final var globalEnabledAnnotation = parameter.getAnnotation(GlobalEnabled.class);

		var globalEnabled = true;
		if (globalEnabledAnnotation != null) {
			globalEnabled = globalEnabledAnnotation.value();
		}

		return getDatabaseManager(client, tables, historyTable, globalEnabled, hashed, classPath, "parallelIndex");
	}

	@Override
	public void beforeEach(ExtensionContext extensionContext) throws Exception {
		var store = extensionContext.getStore(Namespace.create(extensionContext.getUniqueId()));
		var test = store;
		final var testMethod = extensionContext.getRequiredTestMethod();
		final var testDatabase = testMethod.getAnnotation(TestDatabase.class);
		var wrapper = HOLDER.getServer();

		test.put("table", wrapper);

		final var client = wrapper.client;
		final var streamClient = wrapper.streamClient;

		System.setProperty("sqlite4java.library.path", "native-libs");

		var finished = new CompletableFuture<>();
		store.put("future", finished);

		var organisationId = testDatabase.organisationId();
		var classPath = testDatabase.classPath();
		var hashed = testDatabase.hashed();

		final var withHistory = Arrays
			.stream(testMethod.getParameters())
			.map(parameter -> parameter.getType().isAssignableFrom(HistoryProcessor.class))
			.filter(p -> p)
			.findFirst()
			.orElse(false);

		final var argumentsList = Arrays
			.stream(testMethod.getParameters())
			.map(parameter -> {
				try {
					if (parameter.getType().isAssignableFrom(DynamoDbManager.class)) {
						return createDynamoDbManager(client, streamClient, parameter, withHistory, hashed, classPath);
					} else if (parameter.getType().isAssignableFrom(HistoryProcessor.class)) {
						return new HistoryProcessor(client, streamClient, parameter, organisationId);
					} else {
						return createDatabase(client, streamClient, parameter, organisationId, withHistory, hashed, classPath, finished);
					}
				} catch (final Exception e) {
					e.printStackTrace();
					throw new ExceptionInInitializerError("Could not build parameters");
				}
			})
			.collect(Collectors.toList());
		store.put("arguments", argumentsList);
	}

	@Override
	public void afterEach(ExtensionContext context) throws Exception {
		var wrapper = context.getStore(Namespace.create(context.getUniqueId())).get("table", ServerWrapper.class);
		CompletableFuture<?> future = context.getStore(Namespace.create(context.getUniqueId())).get("future", CompletableFuture.class);

		if (future != null) {
			future.complete(null);
		}
		if (wrapper != null) {
			HOLDER.returnServer(wrapper);
		}
	}

	static class DbHolder {

		private final ConcurrentLinkedQueue<ServerWrapper> servers;

		public DbHolder() {
			this.servers = new ConcurrentLinkedQueue<>();
		}

		ServerWrapper getServer() throws Exception {
			var wrapper = this.servers.poll();
			if (wrapper == null) {
				final String port = findFreePort();

				startDynamoServer(port);
				final var client = startDynamoClient(port);
				final var streamClient = startDynamoStreamClient(port);

				return new ServerWrapper(client, streamClient);
			} else {
				var tables = wrapper.client.listTables().get();
				CompletableFutureUtil.sequence(tables.tableNames().stream().map(table -> wrapper.client.deleteTable(r -> r.tableName(table)))).get();
				return wrapper;
			}
		}

		void returnServer(ServerWrapper wrapper) {
			this.servers.add(wrapper);
		}
	}

	static class ServerWrapper {

		private final DynamoDbAsyncClient client;
		private final DynamoDbStreamsAsyncClient streamClient;

		public ServerWrapper(DynamoDbAsyncClient client, DynamoDbStreamsAsyncClient streamClient) {
			this.client = client;
			this.streamClient = streamClient;
		}
	}
}
