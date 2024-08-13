package com.fleetpin.graphql.database.manager;

import com.fleetpin.graphql.database.manager.access.ModificationPermission;
import java.util.concurrent.CompletableFuture;

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
}
