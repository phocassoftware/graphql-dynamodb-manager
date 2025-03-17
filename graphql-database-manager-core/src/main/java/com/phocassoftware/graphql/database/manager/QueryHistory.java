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

import java.time.Instant;
import java.util.Objects;

public class QueryHistory<T extends Table> {

	private final Class<T> type;
	private final String startsWith;
	private final String id;
	private final Long fromRevision;
	private final Instant fromUpdatedAt;
	private final Long toRevision;
	private final Instant toUpdatedAt;

	QueryHistory(Class<T> type, String startsWith, String id, Long fromRevision, Long toRevision, Instant fromUpdatedAt, Instant toUpdatedAt) {
		if (type == null) {
			throw new RuntimeException("type can not be null, did you forget to call .on(Table::class)?");
		}
		this.type = type;
		this.startsWith = startsWith;
		this.id = id;
		this.fromRevision = fromRevision;
		this.fromUpdatedAt = fromUpdatedAt;
		this.toRevision = toRevision;
		this.toUpdatedAt = toUpdatedAt;
	}

	public Class<T> getType() {
		return type;
	}

	public String getStartsWith() {
		return startsWith;
	}

	public String getId() {
		return id;
	}

	public Long getFromRevision() {
		return fromRevision;
	}

	public Instant getFromUpdatedAt() {
		return fromUpdatedAt;
	}

	public Long getToRevision() {
		return toRevision;
	}

	public Instant getToUpdatedAt() {
		return toUpdatedAt;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fromRevision, fromUpdatedAt, id, startsWith, toRevision, toUpdatedAt, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		QueryHistory other = (QueryHistory) obj;
		return (Objects.equals(fromRevision, other.fromRevision) &&
			Objects.equals(fromUpdatedAt, other.fromUpdatedAt) &&
			Objects.equals(id, other.id) &&
			Objects.equals(startsWith, other.startsWith) &&
			Objects.equals(toRevision, other.toRevision) &&
			Objects.equals(toUpdatedAt, other.toUpdatedAt) &&
			Objects.equals(type, other.type));
	}
}
