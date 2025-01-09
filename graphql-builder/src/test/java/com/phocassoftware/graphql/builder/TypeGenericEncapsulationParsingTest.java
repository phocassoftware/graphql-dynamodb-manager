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

import graphql.ExecutionResult;
import graphql.GraphQL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypeGenericEncapsulationParsingTest {
	@Test
	public void testCatName() throws ReflectiveOperationException {
		var name = getField("Cat", "OBJECT", "name");
		var nonNull = confirmNonNull(name);
		confirmString(nonNull);
	}

	@Test
	public void testCatFur() throws ReflectiveOperationException {
		var name = getField("Cat", "OBJECT", "fur");
		var nonNull = confirmNonNull(name);
		confirmObject(nonNull, "CatFur");
	}

	@Test
	public void testCatFurs() throws ReflectiveOperationException {
		var name = getField("Cat", "OBJECT", "furs");
		var type = confirmNonNull(name);
		type = confirmArray(type);
		type = confirmNonNull(type);
		confirmObject(type, "CatFur");
	}

	@Test
	public void testDogName() throws ReflectiveOperationException {
		var name = getField("Dog", "OBJECT", "name");
		var nonNull = confirmNonNull(name);
		confirmString(nonNull);
	}

	@Test
	public void testDogFur() throws ReflectiveOperationException {
		var name = getField("Dog", "OBJECT", "fur");
		var nonNull = confirmNonNull(name);
		confirmObject(nonNull, "DogFur");
	}

	@Test
	public void testDogFurs() throws ReflectiveOperationException {
		var name = getField("Dog", "OBJECT", "furs");
		var type = confirmNonNull(name);
		type = confirmArray(type);
		type = confirmNonNull(type);
		confirmObject(type, "DogFur");
	}

	private void confirmString(Map<String, Object> type) {
		Assertions.assertEquals("SCALAR", type.get("kind"));
		Assertions.assertEquals("String", type.get("name"));
	}

	private void confirmInterface(Map<String, Object> type, String name) {
		Assertions.assertEquals("INTERFACE", type.get("kind"));
		Assertions.assertEquals(name, type.get("name"));
	}

	private void confirmObject(Map<String, Object> type, String name) {
		Assertions.assertEquals("OBJECT", type.get("kind"));
		Assertions.assertEquals(name, type.get("name"));
	}

	private void confirmBoolean(Map<String, Object> type) {
		Assertions.assertEquals("SCALAR", type.get("kind"));
		Assertions.assertEquals("Boolean", type.get("name"));
	}

	private void confirmNumber(Map<String, Object> type) {
		Assertions.assertEquals("SCALAR", type.get("kind"));
		Assertions.assertEquals("Int", type.get("name"));
	}

	private Map<String, Object> confirmNonNull(Map<String, Object> type) {
		Assertions.assertEquals("NON_NULL", type.get("kind"));
		var toReturn = (Map<String, Object>) type.get("ofType");
		Assertions.assertNotNull(toReturn);
		return toReturn;
	}

	private Map<String, Object> confirmArray(Map<String, Object> type) {
		Assertions.assertEquals("LIST", type.get("kind"));
		var toReturn = (Map<String, Object>) type.get("ofType");
		Assertions.assertNotNull(toReturn);
		return toReturn;
	}

	public Map<String, Object> getField(String typeName, String kind, String name) throws ReflectiveOperationException {
		Map<String, Map<String, Object>> response = execute(
			"{" +
			"  __type(name: \"" +
			typeName +
			"\") {" +
			"    name" +
			"    kind" +
			"    fields {" +
			"      name" +
			"      type {" +
			"        name" +
			"        kind" +
			"        ofType {" +
			"          name" +
			"          kind" +
			"          ofType {" +
			"            name" +
			"            kind" +
			"            ofType {" +
			"              name" +
			"              kind" +
			"            }" +
			"          }" +
			"        }" +
			"      }" +
			"    }" +
			"  }" +
			"} "
		)
			.getData();
		var type = response.get("__type");
		Assertions.assertEquals(typeName, type.get("name"));
		Assertions.assertEquals(kind, type.get("kind"));

		List<Map<String, Object>> fields = (List<Map<String, Object>>) type.get("fields");
		var field = fields.stream().filter(map -> map.get("name").equals(name)).findAny().get();
		Assertions.assertEquals(name, field.get("name"));
		return (Map<String, Object>) field.get("type");
	}

	@Test
	public void testQueryCatFur() throws ReflectiveOperationException {
		Map<String, Map<String, Object>> response = execute(
			"query { " +
				"getCat {" +
					"   name " +
					"   fur{ " +
					"    calico " +
					"    length" +
					"    long" +
					"   }" +
					"  } " +
				"} "
		)
			.getData();

		var cat = response.get("getCat");
		var catFur = (Map<String, Object>) cat.get("fur");

		assertEquals("name", cat.get("name"));
		assertEquals(4, catFur.get("length"));
		assertEquals(true, catFur.get("calico"));

	}

	private ExecutionResult execute(String query) {
		GraphQL schema = GraphQL.newGraphQL(SchemaBuilder.build("com.phocassoftware.graphql.builder.generics.encapsulation")).build();
		ExecutionResult result = schema.execute(query);
		if (!result.getErrors().isEmpty()) {
			throw new RuntimeException(result.getErrors().toString());
		}
		return result;
	}
}
