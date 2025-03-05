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

public interface KeyFactory {
	static <T extends Table> DatabaseKey<T> createDatabaseKey(final String organisationId, final Class<T> type, final String id) {
		return new DatabaseKey<>(organisationId, type, id);
	}

	static <T extends Table> DatabaseQueryKey<T> createDatabaseQueryKey(final String organisationId, final Query<T> query) {
		return new DatabaseQueryKey<>(organisationId, query);
	}

	static <T extends Table> DatabaseQueryHistoryKey<T> createDatabaseQueryHistoryKey(String organisationId, QueryHistory<T> queryHistory) {
		return new DatabaseQueryHistoryKey<>(organisationId, queryHistory);
	}
}
