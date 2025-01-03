package com.phocas.graphql.database.manager.test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.phocas.graphql.database.manager.DataWriter;
import com.phocas.graphql.database.manager.DatabaseDriver;
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
