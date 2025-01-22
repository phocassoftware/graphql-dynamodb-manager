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

package com.phocassoftware.graphql.database.manager.virtual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.phocassoftware.graphql.builder.annotations.Context;
import com.phocassoftware.graphql.database.manager.VirtualDatabase;
import graphql.schema.DataFetcher;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unchecked")
public class VirtualDataRunnerTest {

	@Test
	public void testNormalMethod() throws Exception {
		var runner = new VirtualDataRunner();

		DataFetcher<?> fetcher = g -> "test";

		var replacement = runner.manage(String.class.getMethod("toLowerCase"), fetcher);

		// no context or database so returns itself
		assertEquals(fetcher, replacement);

		assertEquals("test", replacement.get(null));
	}

	@Test
	public void testContext() throws Exception {
		var runner = new VirtualDataRunner();

		DataFetcher<?> fetcher = g -> "test";

		var replacement = runner.manage(VirtualDataRunnerTest.class.getMethod("needsContext", TestContext.class), fetcher);

		assertNotEquals(fetcher, replacement);

		CompletableFuture<String> future = (CompletableFuture<String>) replacement.get(null);

		assertEquals("test", future.join());
	}

	@Test
	public void testDatabase() throws Exception {
		var runner = new VirtualDataRunner();

		DataFetcher<?> fetcher = g -> "test";

		var replacement = runner.manage(VirtualDataRunnerTest.class.getMethod("needsDb", VirtualDatabase.class), fetcher);

		assertNotEquals(fetcher, replacement);

		CompletableFuture<String> future = (CompletableFuture<String>) replacement.get(null);

		assertEquals("test", future.join());
	}

	@Test
	public void testException() throws Exception {
		var runner = new VirtualDataRunner();

		var exception = new RuntimeException("failed");
		DataFetcher<?> fetcher = g -> {
			throw exception;
		};

		var replacement = runner.manage(VirtualDataRunnerTest.class.getMethod("needsDb", VirtualDatabase.class), fetcher);

		assertNotEquals(fetcher, replacement);

		CompletableFuture<String> future = (CompletableFuture<String>) replacement.get(null);

		var got = assertThrows(CompletionException.class, future::join);
		assertEquals(exception, got.getCause());
	}

	@Context
	record TestContext() {}

	public static String needsContext(TestContext context) {
		return "";
	}

	public static String needsDb(VirtualDatabase db) {
		return "";
	}
}
