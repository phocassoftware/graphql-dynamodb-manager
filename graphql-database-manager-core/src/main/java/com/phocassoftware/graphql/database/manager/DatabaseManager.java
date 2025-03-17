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
package com.phocassoftware.graphql.database.manager;

import com.phocassoftware.graphql.database.manager.access.ModificationPermission;
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
