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
