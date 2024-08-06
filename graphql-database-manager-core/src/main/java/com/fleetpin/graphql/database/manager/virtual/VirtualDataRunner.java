package com.fleetpin.graphql.database.manager.virtual;

import com.fleetpin.graphql.builder.DataFetcherRunner;
import com.fleetpin.graphql.builder.annotations.Context;
import com.fleetpin.graphql.database.manager.VirtualDatabase;
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
								throw new RuntimeException(e);
							}
						},
						VirtualDatabase.VIRTUAL_THREAD_POOL
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
