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

package com.phocas.graphql.database.dynamo.history.lambda;

import static java.util.stream.Collectors.groupingBy;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.transformers.v2.DynamodbEventTransformer;
import com.phocas.graphql.database.manager.dynamo.HistoryUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.Record;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

public abstract class HistoryLambda implements RequestHandler<DynamodbEvent, Void> {

	public HistoryLambda() {}

	public abstract String getTableName();

	public abstract DynamoDbClient getClient();

	@Override
	public Void handleRequest(DynamodbEvent input, Context context) {
		var records = DynamodbEventTransformer.toRecordsV2(input);
		process(records);
		return null;
	}

	public void process(List<Record> records) {
		int chunkSize = 25;

		AtomicInteger counter = new AtomicInteger();
		var chunks = HistoryUtil
			.toHistoryValue(records.stream())
			.map(item -> WriteRequest.builder().putRequest(builder -> builder.item(item)).build())
			.collect(groupingBy(x -> counter.getAndIncrement() / chunkSize))
			.values();

		chunks
			.parallelStream()
			.filter(chunk -> !chunk.isEmpty())
			.forEach(chunk -> {
				var items = new HashMap<String, List<WriteRequest>>();
				items.put(getTableName(), chunk);
				writeItems(items);
			});
	}

	private void writeItems(Map<String, List<WriteRequest>> items) {
		var response = getClient().batchWriteItem(builder -> builder.requestItems(items));

		var unprocessed = response.unprocessedItems();

		if (!unprocessed.isEmpty()) {
			writeItems(unprocessed);
		}
	}
}
