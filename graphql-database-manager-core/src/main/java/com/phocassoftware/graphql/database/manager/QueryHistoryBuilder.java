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

import com.phocassoftware.graphql.database.manager.util.HistoryCoreUtil;
import java.time.Instant;
import java.util.function.Consumer;

public class QueryHistoryBuilder<V extends Table> {

	private final Class<V> type;
	private String startsWith;
	private String id;
	private Long fromRevision;
	private Long toRevision;
	private Instant fromUpdatedAt;
	private Instant toUpdatedAt;

	private QueryHistoryBuilder(Class<V> type) {
		this.type = type;
	}

	public QueryHistoryBuilder<V> startsWith(String prefix) {
		this.startsWith = prefix;
		return this;
	}

	public QueryHistoryBuilder<V> id(String id) {
		this.id = id;
		return this;
	}

	public QueryHistoryBuilder<V> fromRevision(Long fromRevision) {
		this.fromRevision = fromRevision;
		return this;
	}

	public QueryHistoryBuilder<V> toRevision(Long toRevision) {
		this.toRevision = toRevision;
		return this;
	}

	public QueryHistoryBuilder<V> fromUpdatedAt(Instant fromUpdatedAt) {
		this.fromUpdatedAt = fromUpdatedAt;
		return this;
	}

	public QueryHistoryBuilder<V> toUpdatedAt(Instant toUpdatedAt) {
		this.toUpdatedAt = toUpdatedAt;
		return this;
	}

	public QueryHistoryBuilder<V> applyMutation(Consumer<QueryHistoryBuilder<V>> mutator) {
		mutator.accept((QueryHistoryBuilder<V>) this);
		return (QueryHistoryBuilder<V>) this;
	}

	public QueryHistory<V> build() {
		checkArgument(HistoryCoreUtil.hasHistory(type), "Can only do history when history annotation is present.");
		checkArgument(!(id != null && startsWith != null), "ID and StartsWith cannot both be set.");
		checkArgument(!(id == null && startsWith == null), "ID or StartsWith must be set.");
		if (fromRevision != null || toRevision != null) {
			checkArgument(fromUpdatedAt == null && toUpdatedAt == null, "Revision and CreatedAt cannot both be set.");
			checkArgument(startsWith == null, "StartsWith can only be used with updatedAt.");
		}
		return new QueryHistory<V>(type, startsWith, id, fromRevision, toRevision, fromUpdatedAt, toUpdatedAt);
	}

	private void checkArgument(boolean pass, String msg) {
		if (!pass) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static <V extends Table> QueryHistoryBuilder<V> create(Class<V> type) {
		return new QueryHistoryBuilder<V>(type);
	}
}
