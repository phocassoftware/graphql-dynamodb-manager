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
