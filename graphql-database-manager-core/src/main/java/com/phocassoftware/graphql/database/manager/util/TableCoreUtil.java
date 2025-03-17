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
