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
package com.phocas.graphql.database.manager;

import com.phocas.graphql.database.manager.ScanResult.Item;
import com.phocas.graphql.database.manager.ScanUpdater.ScanContext;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TableScanner {

	private final TableScanQuery query;
	private final DatabaseDriver driver;
	private final DatabaseManager databaseManager;

	public TableScanner(TableScanQuery query, DatabaseDriver driver, DatabaseManager databaseManager) {
		this.query = query;
		this.driver = driver;
		this.databaseManager = databaseManager;
	}

	public CompletableFuture<Void> start() {
		var workers = new ArrayList<CompletableFuture<ScannerWorker>>();
		for (int i = 0; i < query.parallelism(); i++) {
			var segment = i;
			workers.add(CompletableFuture.supplyAsync(new ScannerWorker(segment), Database.VIRTUAL_THREAD_POOL));
		}
		return CompletableFuture.allOf(workers.toArray(CompletableFuture[]::new));
	}

	private <T extends Table> CompletableFuture<Void> process(Item<T> item) {
		for (var updater : query.updaters()) {
			if (updater.type().isAssignableFrom(item.entity().getClass())) {
				@SuppressWarnings("unchecked")
				ScanUpdater<T> update = (ScanUpdater<T>) updater;
				return CompletableFuture.runAsync(
					() -> {
						var virtualDatabase = databaseManager.getVirtualDatabase(item.organisationId());
						update.updater().accept(new ScanContext<T>(virtualDatabase, item), item.entity());
					},
					Database.VIRTUAL_THREAD_POOL
				);
			}
		}
		return CompletableFuture.completedFuture(null);
	}

	private class ScannerWorker implements Supplier<ScannerWorker> {

		private final int segment;

		public ScannerWorker(int segment) {
			this.segment = segment;
		}

		@Override
		public ScannerWorker get() {
			Object from = null;
			do {
				var scan = driver.startTableScan(query, segment, from);
				if (query.monitor() != null) {
					query.monitor().onScanSegmentStart(segment, scan.items().size(), from);
				}
				from = scan.next();

				var workers = new ArrayList<CompletableFuture<Void>>();
				for (var item : scan.items()) {
					workers.add(process(item));
				}

				CompletableFuture.allOf(workers.toArray(CompletableFuture[]::new)).join();
			} while (from != null);
			if (query.monitor() != null) {
				query.monitor().onScanSegmentComplete(segment);
			}

			return this;
		}
	}
}
