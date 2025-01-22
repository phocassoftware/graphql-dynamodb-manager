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
package com.phocassoftware.graphql.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import graphql.ExceptionWhileDataFetching;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class TypeGenericInputRecords {

	@Test
	public void textQuery() throws ReflectiveOperationException {
		Map<String, String> response = execute("""
			query {doChange(input: {
				name: { 
					wrap: ["felix"]
				},
				age: {
					wrap: [234]
				},
				description: {
					wrap: "cat"
				}
			})}
			""")
			.getData();
		var change = response.get("doChange");
		assertEquals("felix[234]cat", change);
	}

	@Test
	public void textCorrectNullableQuery() throws ReflectiveOperationException {
		Map<String, String> response = execute("""
			query {doChange(input: {
				name: {
					wrap: ["felix"]
				},
				age: {
				},
				description: {
					wrap: "cat"
				}
			})}
			""").getData();
		var change = response.get("doChange");
		assertEquals("felixnullcat", change);
	}

	@Test
	public void textQueryNull() throws ReflectiveOperationException {
		Map<String, String> response = execute("""
			query {doChange(input: {})}
			""").getData();
		var change = response.get("doChange");
		assertEquals("empty", change);
	}

	private ExecutionResult execute(String query) {
		GraphQL schema = GraphQL.newGraphQL(SchemaBuilder.build("com.phocassoftware.graphql.builder.inputgenericsRecords")).build();
		ExecutionResult result = schema.execute(query);
		if (!result.getErrors().isEmpty()) {
			ExceptionWhileDataFetching d = (ExceptionWhileDataFetching) result.getErrors().get(0);
			d.getException().printStackTrace();
			throw new RuntimeException(result.getErrors().toString());
		}
		return result;
	}
}
