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

import com.phocassoftware.graphql.builder.DataFetcherRunner;
import com.phocassoftware.graphql.builder.annotations.Context;
import com.phocassoftware.graphql.database.manager.Database;
import com.phocassoftware.graphql.database.manager.VirtualDatabase;
import graphql.schema.DataFetcher;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class VirtualDataRunner implements DataFetcherRunner {

	@Override
	public DataFetcher<?> manage(Method method, DataFetcher<?> fetcher) {
		for (var parameter : method.getParameterTypes()) {
			if (parameter.isAssignableFrom(VirtualDatabase.class) || hasContext(parameter)) {
				var isCompletableFuture = CompletionStage.class.isAssignableFrom(method.getReturnType());
				return env -> {
					var result = CompletableFuture.supplyAsync(
						() -> {
							try {
								return fetcher.get(env);
							} catch (Exception e) {
								if (e instanceof RuntimeException runtime) {
									throw runtime;
								}
								throw new RuntimeException(e);
							}
						},
						Database.VIRTUAL_THREAD_POOL
					);

					if (isCompletableFuture) {
						return result.thenCompose(r -> (CompletionStage<?>) r);
					}

					return result;
				};
			}
		}

		return fetcher;
	}

	private boolean hasContext(Class<?> parameter) {
		if (parameter.isAnnotationPresent(Context.class)) {
			return true;
		}

		for (var inter : parameter.getInterfaces()) {
			if (hasContext(inter)) {
				return true;
			}
		}
		var parent = parameter.getSuperclass();
		if (parent != null && hasContext(parent)) {
			return true;
		}
		return false;
	}
}
