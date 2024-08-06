package com.fleetpin.graphql.database.manager.test;

import com.fleetpin.graphql.database.manager.Table;
import com.fleetpin.graphql.database.manager.VirtualDatabase;
import com.fleetpin.graphql.database.manager.test.annotations.TestDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Assertions;

public class VirtualDatabaseTest {

	@TestDatabase
	public void testConcurrency(VirtualDatabase database) throws InterruptedException, ExecutionException {
		List<CompletableFuture<?>> futures = new ArrayList<>();

		for (int i = 0; i < 100; i++) {
			var f = i;
			var future = CompletableFuture.supplyAsync(
				() -> {
					var fc = new Simple();
					fc.setId("a:b:c:" + f);
					return database.put(fc);
				},
				VirtualDatabase.VIRTUAL_THREAD_POOL
			);
			futures.add(future);
		}

		futures.forEach(CompletableFuture::join);

		futures.clear();

		for (int i = 0; i < 100; i++) {
			var f = i;
			var future = CompletableFuture.supplyAsync(
				() -> {
					var fc = database.get(Simple.class, "a:b:c:" + f);
					Assertions.assertNotNull(fc);
					return fc;
				},
				VirtualDatabase.VIRTUAL_THREAD_POOL
			);
			futures.add(future);
		}

		futures.forEach(CompletableFuture::join);
	}

	static class Simple extends Table {}
}
