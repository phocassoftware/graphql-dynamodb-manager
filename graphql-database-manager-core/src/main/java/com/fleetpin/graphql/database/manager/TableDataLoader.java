package com.fleetpin.graphql.database.manager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.dataloader.DataLoader;

public class TableDataLoader<K> {

	private final DataLoader<K, ?> loader;
	private final Consumer<CompletableFuture<?>> handleFuture;

	TableDataLoader(DataLoader<K, ?> loader, Consumer<CompletableFuture<?>> handleFuture) {
		this.loader = loader;
		this.handleFuture = handleFuture;
	}

	public <T> CompletableFuture<T> load(K key) {
		var future = (CompletableFuture<T>) loader.load(key);
		this.handleFuture.accept(future);
		return future;
	}

	public <T> CompletableFuture<List<T>> loadMany(List<K> keys) {
		// annoying waste of memory/cpu to get around cast :(
		var future = loader.loadMany(keys).thenApply(r -> r.stream().map(t -> (T) t).collect(Collectors.toList()));
		this.handleFuture.accept(future);
		return future;
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
