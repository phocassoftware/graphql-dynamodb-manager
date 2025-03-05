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
package com.phocassoftware.graphql.database.manager.test.hashed;

import com.phocassoftware.graphql.database.manager.PutValue;
import com.phocassoftware.graphql.database.manager.RevisionMismatchException;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Assertions;

public class DynamoDbPutValueTest {

	@TestDatabase
	void testSuccess() {
		DynamoDbIndexesTest.SimpleTable entry1 = new DynamoDbIndexesTest.SimpleTable("garry", "john");

		var completableFuture = new CompletableFuture<DynamoDbIndexesTest.SimpleTable>();
		var putValue = new PutValue<DynamoDbIndexesTest.SimpleTable>("test", entry1, false, completableFuture);

		Assertions.assertEquals(false, putValue.getFuture().isDone());
		Assertions.assertEquals(0, putValue.getEntity().getRevision());

		putValue.resolve();

		Assertions.assertEquals(true, putValue.getFuture().isDone());
		Assertions.assertEquals(1, putValue.getEntity().getRevision());
	}

	@TestDatabase
	void testFailure() {
		DynamoDbIndexesTest.SimpleTable entry1 = new DynamoDbIndexesTest.SimpleTable("garry", "john");

		var completableFuture = new CompletableFuture<DynamoDbIndexesTest.SimpleTable>();
		var putValue = new PutValue<DynamoDbIndexesTest.SimpleTable>("test", entry1, false, completableFuture);

		Assertions.assertEquals(false, putValue.getFuture().isDone());
		Assertions.assertEquals(0, putValue.getEntity().getRevision());

		putValue.fail(new RevisionMismatchException(new Exception("hello")));

		Assertions.assertEquals(true, putValue.getFuture().isDone());
		Assertions.assertEquals(true, putValue.getFuture().isCompletedExceptionally());
		Assertions.assertEquals(0, putValue.getEntity().getRevision());
	}
}
