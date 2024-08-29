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
package com.fleetpin.graphql.database.manager;

import com.fleetpin.graphql.database.manager.ScanUpdater.ScanContext;
import com.fleetpin.graphql.database.manager.TableScanQuery.TableScanMonitor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class TableScanQueryBuilder {

	private int parallelism = Runtime.getRuntime().availableProcessors() * 5;
	private List<ScanUpdater<?>> updaters = new ArrayList<>();
	private TableScanMonitor monitor;

	public TableScanQueryBuilder parallelism(int parallelism) {
		this.parallelism = parallelism;
		return this;
	}

	public TableScanQueryBuilder updater(ScanUpdater<?> updater) {
		updaters.add(updater);
		return this;
	}

	public <T extends Table> TableScanQueryBuilder updater(Class<T> type, BiConsumer<ScanContext<T>, T> updater) {
		updaters.add(new ScanUpdater<T>(type, updater));
		return this;
	}

	public TableScanQuery build() {
		return new TableScanQuery(monitor, parallelism, updaters);
	}
}
