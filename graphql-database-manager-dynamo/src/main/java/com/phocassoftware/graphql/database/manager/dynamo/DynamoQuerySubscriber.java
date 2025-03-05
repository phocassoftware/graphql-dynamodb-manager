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
package com.phocassoftware.graphql.database.manager.dynamo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

public class DynamoQuerySubscriber implements Subscriber<QueryResponse> {

	private final ArrayList<DynamoItem> stuff;
	private final AtomicInteger togo;
	private Subscription s;
	private final CompletableFuture<List<DynamoItem>> future = new CompletableFuture<List<DynamoItem>>();
	private final String table;

	protected DynamoQuerySubscriber(String table) {
		this(table, null);
	}

	protected DynamoQuerySubscriber(String table, Integer limit) {
		this.table = table;

		if (limit != null) {
			this.togo = new AtomicInteger(limit);
			this.stuff = new ArrayList<>(limit);
		} else {
			this.togo = null;
			this.stuff = new ArrayList<>();
		}
	}

	@Override
	public void onSubscribe(Subscription s) {
		this.s = s;
		s.request(1);
	}

	@Override
	public void onNext(QueryResponse r) {
		try {
			var stream = r.items().stream();

			if (togo != null) {
				stream = stream.takeWhile(__ -> togo.getAndDecrement() >= 0);
			}

			stream.map(item -> new DynamoItem(this.table, item)).forEach(stuff::add);

			if (togo == null || togo.get() > 0) {
				this.s.request(1);
			} else {
				s.cancel();
				this.onComplete();
			}
		} catch (Exception e) {
			this.onError(e);
			s.cancel();
		}
	}

	@Override
	public void onError(Throwable t) {
		future.completeExceptionally(t);
	}

	@Override
	public void onComplete() {
		future.complete(stuff);
	}

	public CompletableFuture<List<DynamoItem>> getFuture() {
		return this.future;
	}
}
