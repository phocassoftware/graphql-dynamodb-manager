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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.dataloader.DataLoader;

public class TableDataLoader<K> {

	private final DataLoader<K, ?> loader;
	private final Function<CompletableFuture<?>, CompletableFuture<?>> handleFuture;

	TableDataLoader(DataLoader<K, ?> loader, Function<CompletableFuture<?>, CompletableFuture<?>> handleFuture) {
		this.loader = loader;
		this.handleFuture = handleFuture;
	}

	public <T> CompletableFuture<T> load(K key) {
		var future = loader.load(key);
		return (CompletableFuture<T>) this.handleFuture.apply(future);
	}

	public <T> CompletableFuture<List<T>> loadMany(List<K> keys) {
		// annoying waste of memory/cpu to get around cast :(
		var future = loader.loadMany(keys).thenApply(r -> r.stream().map(t -> (T) t).collect(Collectors.toList()));
		return (CompletableFuture<List<T>>) this.handleFuture.apply(future);
	}

	public void clear(K key) {
		loader.clear(key);
	}

	public void clearAll() {
		loader.clearAll();
	}

	public int dispatchDepth() {
		return loader.dispatchDepth();
	}

	public CompletableFuture dispatch() {
		return loader.dispatch();
	}
}
