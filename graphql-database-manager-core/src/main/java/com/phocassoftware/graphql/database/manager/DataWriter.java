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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class DataWriter {

	private final Function<List<PutValue>, CompletableFuture<Void>> bulkWriter;
	private final List<PutValue> toPut = new ArrayList<>();
	private final Consumer<CompletableFuture<?>> handleFuture;

	public DataWriter(Function<List<PutValue>, CompletableFuture<Void>> bulkWriter, Consumer<CompletableFuture<?>> handleFuture) {
		this.bulkWriter = bulkWriter;
		this.handleFuture = handleFuture;
	}

	public int dispatchSize() {
		synchronized (toPut) {
			return toPut.size();
		}
	}

	public CompletableFuture<Void> dispatch() {
		List<PutValue> toSend = null;
		synchronized (toPut) {
			if (!toPut.isEmpty()) {
				toSend = new ArrayList<>(toPut);
				toPut.clear();
			}
		}
		if (toSend == null) {
			return CompletableFuture.completedFuture(null);
		} else {
			return bulkWriter.apply(toSend);
		}
	}

	public <T extends Table> CompletableFuture<T> put(String organisationId, T entity, boolean check) {
		var future = new CompletableFuture<T>();
		var putValue = new PutValue<T>(organisationId, entity, check, future);
		synchronized (toPut) {
			toPut.add(putValue);
		}
		handleFuture.accept(future);
		return future;
	}
}
