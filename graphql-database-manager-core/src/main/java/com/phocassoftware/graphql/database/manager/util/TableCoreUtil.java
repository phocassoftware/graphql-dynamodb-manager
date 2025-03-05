package com.phocassoftware.graphql.database.manager.util;

import com.phocassoftware.graphql.database.manager.Table;
import com.phocassoftware.graphql.database.manager.annotations.TableName;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public final class TableCoreUtil {

	public static String table(Class<? extends Table> type) {
		var tmp = baseClass(type);
		var name = tmp.getDeclaredAnnotation(TableName.class);
		if (name == null) {
			return type.getSimpleName().toLowerCase() + "s";
		} else {
			return name.value();
		}
	}

	public static <T extends Table> Class<T> baseClass(Class<T> type) {
		Class<?> tmp = type;
		TableName name = null;
		while (name == null && tmp != null) {
			name = tmp.getDeclaredAnnotation(TableName.class);
			if (name != null) {
				return (Class<T>) tmp;
			}
			tmp = tmp.getSuperclass();
		}
		return type;
	}

	public static <T> CompletableFuture<List<T>> all(List<CompletableFuture<T>> collect) {
		return CompletableFuture
			.allOf(collect.toArray(CompletableFuture[]::new))
			.thenApply(
				__ -> collect
					.stream()
					.map(m -> {
						try {
							return m.get();
						} catch (InterruptedException | ExecutionException e) {
							throw new RuntimeException(e);
						}
					})
					.collect(Collectors.toList())
			);
	}
}
