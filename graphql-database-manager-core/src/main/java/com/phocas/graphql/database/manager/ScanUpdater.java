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
import java.util.function.BiConsumer;

public record ScanUpdater<T extends Table>(Class<?> type, BiConsumer<ScanContext<T>, T> updater) {
	public static class ScanContext<T extends Table> {

		private VirtualDatabase virtualDatabase;
		private Item<T> item;

		protected ScanContext(VirtualDatabase virtualDatabase, Item<T> item) {
			this.virtualDatabase = virtualDatabase;
			this.item = item;
		}

		public VirtualDatabase getVirtualDatabase() {
			return virtualDatabase;
		}

		public void delete() {
			item.delete().accept(item.entity());
		}

		public void replace(T entity) {
			item.replace().accept(entity);
		}
	}
}
