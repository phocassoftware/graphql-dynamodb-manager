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
package com.fleetpin.graphql.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TypeInheritanceParsingTest {

	@Test
	public void findTypes() throws ReflectiveOperationException {
		Map<String, Map<String, List<Map<String, String>>>> response = execute("{__schema {types {name}}} ").getData();
		var types = response.get("__schema").get("types");
		var count = types.stream().filter(map -> map.get("name").equals("SimpleType")).count();
		Assertions.assertEquals(1, count);
	}

	@Test
	public void testAnimalName() throws ReflectiveOperationException {
		var name = getField("Animal", "INTERFACE", "name");
		var nonNull = confirmNonNull(name);
		confirmString(nonNull);
	}

	@Test
	public void testAnimalInputName() throws ReflectiveOperationException {
		var name = getField("AnimalInput", "INPUT_OBJECT", "cat");
		confirmInputObject(name, "CatInput");
	}

	@Test
	public void testCatName() throws ReflectiveOperationException {
		var name = getField("Cat", "OBJECT", "name");
		var nonNull = confirmNonNull(name);
		confirmString(nonNull);
	}

	@Test
	public void testCatAge() throws ReflectiveOperationException {
		var name = getField("Cat", "OBJECT", "age");
		var nonNull = confirmNonNull(name);
		confirmNumber(nonNull);
	}

	@Test
	public void testCatFur() throws ReflectiveOperationException {
		var name = getField("Cat", "OBJECT", "fur");
		confirmBoolean(name);
	}

	@Test
	public void testCatCalico() throws ReflectiveOperationException {
		var name = getField("Cat", "OBJECT", "calico");
		var nonNull = confirmNonNull(name);
		confirmBoolean(nonNull);
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
		confirmString(nonNull);
	}

	@Test
	public void testDogAge() throws ReflectiveOperationException {
		var name = getField("Dog", "OBJECT", "age");
		var nonNull = confirmNonNull(name);
		confirmNumber(nonNull);
	}

	@Test
	public void testCatDescription() throws ReflectiveOperationException {
		var type = getField("Cat", "OBJECT", null);
		Assertions.assertEquals("cat type", type.get("description"));
	}

	@Test
	public void testCatFurDescription() throws ReflectiveOperationException {
		var type = getField("Cat", "OBJECT", null);

		List<Map<String, Object>> fields = (List<Map<String, Object>>) type.get("fields");
		var field = fields.stream().filter(map -> map.get("name").equals("fur")).findAny().get();
		Assertions.assertEquals("get fur", field.get("description"));
	}

	@Test
	public void testCatWeightArgumentDescription() throws ReflectiveOperationException {
		var type = getField("Cat", "OBJECT", null);

		List<Map<String, Object>> fields = (List<Map<String, Object>>) type.get("fields");
		var field = fields.stream().filter(map -> map.get("name").equals("weight")).findAny().get();
		Assertions.assertEquals(null, field.get("description"));

		List<Map<String, Object>> args = (List<Map<String, Object>>) field.get("args");
		var round = args.stream().filter(map -> map.get("name").equals("round")).findAny().get();
		Assertions.assertEquals("whole number", round.get("description"));
	}

	@Test
	public void testMutationDescription() throws ReflectiveOperationException {
		var type = getField("Mutations", "OBJECT", null);

		List<Map<String, Object>> fields = (List<Map<String, Object>>) type.get("fields");
		var field = fields.stream().filter(map -> map.get("name").equals("getCat")).findAny().get();
		Assertions.assertEquals("cat endpoint", field.get("description"));

		List<Map<String, Object>> args = (List<Map<String, Object>>) field.get("args");
		var round = args.stream().filter(map -> map.get("name").equals("age")).findAny().get();
		Assertions.assertEquals("sample", round.get("description"));
	}

	@Test
	public void testCatFurInputDescription() throws ReflectiveOperationException {
		var type = getField("CatInput", "INPUT_OBJECT", null);

		List<Map<String, Object>> fields = (List<Map<String, Object>>) type.get("inputFields");
		var field = fields.stream().filter(map -> map.get("name").equals("fur")).findAny().get();
		Assertions.assertEquals("set fur", field.get("description"));
	}

	@Test
	public void testInputCatDescription() throws ReflectiveOperationException {
		var type = getField("CatInput", "INPUT_OBJECT", null);
		Assertions.assertEquals("cat type", type.get("description"));
	}

	@Test
	public void testInputOneOfDescription() throws ReflectiveOperationException {
		var type = getField("AnimalInput", "INPUT_OBJECT", null);
		Assertions.assertEquals("animal desc", type.get("description"));
		List<Map<String, Object>> fields = (List<Map<String, Object>>) type.get("inputFields");
		var field = fields.stream().filter(map -> map.get("name").equals("dog")).findAny().get();
		Assertions.assertEquals("A dog", field.get("description"));
	}

	private void confirmString(Map<String, Object> type) {
		Assertions.assertEquals("SCALAR", type.get("kind"));
		Assertions.assertEquals("String", type.get("name"));
	}

	private void confirmInputObject(Map<String, Object> type, String name) {
		Assertions.assertEquals("INPUT_OBJECT", type.get("kind"));
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
			"    description" +
			"    fields {" +
			"      name" +
			"      description" +
			"      args {" +
			"        name" +
			"        description" +
			"      }" +
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
			"    inputFields {" +
			"      name" +
			"      description" +
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

		if (name == null) {
			return type;
		}

		List<Map<String, Object>> fields = (List<Map<String, Object>>) type.get("fields");
		if (fields == null) {
			fields = (List<Map<String, Object>>) type.get("inputFields");
		}
		var field = fields.stream().filter(map -> map.get("name").equals(name)).findAny().get();
		Assertions.assertEquals(name, field.get("name"));
		return (Map<String, Object>) field.get("type");
	}

	@Test
	public void testQueryCatFur() throws ReflectiveOperationException {
		Map<String, List<Map<String, Object>>> response = execute(
			"query {animals{" + "name " + "... on Cat { " + "  age " + "  fur " + "  calico " + "} " + "... on Dog {" + " age " + "} " + "}} "
		)
			.getData();

		var animals = response.get("animals");

		var cat = animals.get(0);
		var dog = animals.get(1);

		assertEquals("name", cat.get("name"));
		assertEquals(3, cat.get("age"));
		assertEquals(true, cat.get("fur"));
		assertEquals(true, cat.get("calico"));

		assertEquals("name", dog.get("name"));
		assertEquals(6, dog.get("age"));
	}

	@Test
	public void testQueryDogFur() throws ReflectiveOperationException {
		Map<String, List<Map<String, Object>>> response = execute(
			"query {animals{" + "name " + "... on Cat { " + "  age " + "  calico " + "} " + "... on Dog {" + " age " + " fur " + "} " + "}} "
		)
			.getData();

		var animals = response.get("animals");

		var cat = animals.get(0);
		var dog = animals.get(1);

		assertEquals("name", cat.get("name"));
		assertEquals(3, cat.get("age"));
		assertEquals(true, cat.get("calico"));

		assertEquals("name", dog.get("name"));
		assertEquals(6, dog.get("age"));
		assertEquals("shaggy", dog.get("fur"));
	}

	@Test
	public void testBothFurFails() throws ReflectiveOperationException {
		Assertions.assertThrows(
			RuntimeException.class,
			() -> {
				execute(
					"query {animals{" + "name " + "... on Cat { " + "  age " + "  fur " + "  calico " + "} " + "... on Dog {" + " age " + " fur " + "} " + "}} "
				);
			}
		);
	}

	@Test
	public void testOneOf() throws ReflectiveOperationException {
		Map<String, List<Map<String, Object>>> response = execute(
			"mutation {myAnimals(animals: [" +
			"{cat: {fur: true, calico: false, name: \"socks\", age: 4}}," +
			"{dog: {fur: \"short\", name: \"patches\", age: 5}}" +
			"]){" +
			"name " +
			"... on Cat { " +
			"  age " +
			"  calico " +
			"} " +
			"... on Dog {" +
			" age " +
			" fur " +
			"} " +
			"}} "
		)
			.getData();

		var animals = response.get("myAnimals");

		var cat = animals.get(0);
		var dog = animals.get(1);

		assertEquals("socks", cat.get("name"));
		assertEquals(4, cat.get("age"));
		assertEquals(false, cat.get("calico"));

		assertEquals("patches", dog.get("name"));
		assertEquals(5, dog.get("age"));
		assertEquals("short", dog.get("fur"));
	}

	@Test
	public void testOptionalFieldNotSet() throws ReflectiveOperationException {
		Map<String, List<Map<String, Object>>> response = execute(
			"mutation {myAnimals(animals: [" +
			"{cat: {calico: false, name: \"socks\", age: 4}}," +
			"]){" +
			"name " +
			"... on Cat { " +
			"  age " +
			"  calico " +
			" fur " +
			"} " +
			"... on Dog {" +
			" age " +
			"} " +
			"}} "
		)
			.getData();

		var animals = response.get("myAnimals");

		var cat = animals.get(0);

		assertEquals("socks", cat.get("name"));
		assertEquals(4, cat.get("age"));
		assertEquals(false, cat.get("calico"));
		assertEquals(true, cat.get("fur"));
	}

	@Test
	public void testOptionalFieldNull() throws ReflectiveOperationException {
		Map<String, List<Map<String, Object>>> response = execute(
			"mutation {myAnimals(animals: [" +
			"{cat: {fur: null, calico: false, name: \"socks\", age: 4}}," +
			"]){" +
			"name " +
			"... on Cat { " +
			"  age " +
			"  calico " +
			" fur " +
			"} " +
			"... on Dog {" +
			" age " +
			"} " +
			"}} "
		)
			.getData();

		var animals = response.get("myAnimals");

		var cat = animals.get(0);

		assertEquals("socks", cat.get("name"));
		assertEquals(4, cat.get("age"));
		assertEquals(false, cat.get("calico"));
		assertEquals(null, cat.get("fur"));
	}

	@Test
	public void testOneOfError() throws ReflectiveOperationException {
		var exception = assertThrows(
			RuntimeException.class,
			() ->
				execute(
					"mutation {myAnimals(animals: [" +
					"{cat: {fur: true, calico: false, name: \"socks\", age: 4}," +
					"dog: {fur: \"short\", name: \"patches\", age: 5}}" +
					"]){" +
					"name " +
					"... on Cat { " +
					"  age " +
					"  calico " +
					"} " +
					"... on Dog {" +
					" age " +
					" fur " +
					"} " +
					"}} "
				)
					.getData()
		);

		assertTrue(exception.getMessage().contains("OneOf must only have a single field set"));
	}

	@Test
	public void testOneOfErrorEmpty() throws ReflectiveOperationException {
		var exception = assertThrows(
			RuntimeException.class,
			() ->
				execute(
					"mutation {myAnimals(animals: [" +
					"{}" +
					"]){" +
					"name " +
					"... on Cat { " +
					"  age " +
					"  calico " +
					"} " +
					"... on Dog {" +
					" age " +
					" fur " +
					"} " +
					"}} "
				)
					.getData()
		);

		assertTrue(exception.getMessage().contains("OneOf must only have a single field set"));
	}

	@Test
	public void testOneOfErrorField() throws ReflectiveOperationException {
		var exception = assertThrows(
			RuntimeException.class,
			() ->
				execute(
					"mutation {myAnimals(animals: [" +
					"{cat: {fur: null, calico: false, name: \"socks\", age: 4, error: \"fail\"}}" +
					"]){" +
					"name " +
					"... on Cat { " +
					"  age " +
					"  calico " +
					"} " +
					"... on Dog {" +
					" age " +
					" fur " +
					"} " +
					"}} "
				)
					.getData()
		);
		assertTrue(exception.getMessage().contains("ERROR"));
	}

	private ExecutionResult execute(String query) {
		return execute(query, null);
	}

	private ExecutionResult execute(String query, Map<String, Object> variables) {
		GraphQL schema = GraphQL.newGraphQL(SchemaBuilder.build("com.fleetpin.graphql.builder.type")).build();
		var input = ExecutionInput.newExecutionInput();
		input.query(query);
		if (variables != null) {
			input.variables(variables);
		}
		ExecutionResult result = schema.execute(input);
		if (!result.getErrors().isEmpty()) {
			throw new RuntimeException(result.getErrors().toString()); //TODO:cleanup
		}
		return result;
	}
}
