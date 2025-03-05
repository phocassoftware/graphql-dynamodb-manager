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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.phocassoftware.graphql.database.manager.DataWriter;
import com.phocassoftware.graphql.database.manager.DatabaseDriver;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

final class DynamoDbDataWriterTest {

	@TestDatabase
	void testDispatchSize() {
		DatabaseDriver my = Mockito.mock(DatabaseDriver.class, Mockito.CALLS_REAL_METHODS);
		DynamoDbIndexesTest.SimpleTable entry1 = new DynamoDbIndexesTest.SimpleTable("garry", "john");
		var dataWriter = new DataWriter(my::bulkPut, __ -> {});
		dataWriter.put("test", entry1, true);
		Assertions.assertEquals(1, dataWriter.dispatchSize());
	}

	@TestDatabase
	void testDispatch() {
		DatabaseDriver my = Mockito.mock(DatabaseDriver.class, Mockito.CALLS_REAL_METHODS);
		DynamoDbIndexesTest.SimpleTable entry1 = new DynamoDbIndexesTest.SimpleTable("garry", "john");
		var dataWriter = new DataWriter(my::bulkPut, __ -> {});
		dataWriter.put("test", entry1, true);
		dataWriter.dispatch();
		verify(my, times(1)).bulkPut(Mockito.anyList());
	}
}
