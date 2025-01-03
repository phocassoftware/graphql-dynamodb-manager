package com.phocas.graphql.database.manager;

import com.phocas.graphql.database.manager.access.ModificationPermission;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class DatabaseManager {

	private final DatabaseDriver dynamoDb;

	public DatabaseManager(DatabaseDriver dynamoDb) {
		this.dynamoDb = dynamoDb;
	}

	public Database getDatabase(String organisationId) {
		return getDatabase(organisationId, __ -> CompletableFuture.completedFuture(true));
	}

	public Database getDatabase(String organisationId, ModificationPermission putAllow) {
		return new Database(organisationId, dynamoDb, putAllow);
	}

	public VirtualDatabase getVirtualDatabase(String organisationId) {
		return new VirtualDatabase(getDatabase(organisationId));
	}

	public VirtualDatabase getVirtualDatabase(String organisationId, ModificationPermission putAllow) {
		return new VirtualDatabase(getDatabase(organisationId, putAllow));
	}

	public TableScanner startTableScan(Function<TableScanQueryBuilder, TableScanQueryBuilder> builder) {
		return new TableScanner(builder.apply(new TableScanQueryBuilder()).build(), dynamoDb, this);
	}
}
