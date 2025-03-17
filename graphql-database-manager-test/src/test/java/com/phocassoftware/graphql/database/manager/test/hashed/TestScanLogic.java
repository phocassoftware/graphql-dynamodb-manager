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

package com.phocassoftware.graphql.database.manager.test.hashed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.phocassoftware.graphql.database.manager.Table;
import com.phocassoftware.graphql.database.manager.annotations.Hash;
import com.phocassoftware.graphql.database.manager.dynamo.DynamoDbManager;
import java.util.concurrent.ExecutionException;

final class TestScanLogic {

	@TestDatabase
	void testTakeBackup(final DynamoDbManager dynamoDbManager) throws ExecutionException, InterruptedException {
		var org1 = dynamoDbManager.getVirtualDatabase("123");

		org1.put(new Cat("mittens", "loud")).getRevision();
		org1.put(new Cat("boots", "quite"));
		org1.put(new Dog("hash_otis", "small"));

		var org2 = dynamoDbManager.getVirtualDatabase("321");

		org2.put(new Cat("horse", "medium"));
		org2.put(new Dog("hash_dog", "small"));
		org2.put(new Dog("hash_lassy", "loud"));

		var scan = dynamoDbManager
			.startTableScan(
				b -> b
					.updater(
						Cat.class,
						(context, cat) -> {
							if (cat.getName().equals("boots")) {
								context.delete();
							} else if (cat.getName().equals("mittens")) {
								cat.setPur("gone");
								context.replace(cat);
							} else {
								context.getVirtualDatabase().put(cat);
							}
						}
					)
					.updater(
						Dog.class,
						(context, dog) -> {
							if (dog.getName().equals("hash_otis")) {
								context.delete();
							} else if (dog.getName().equals("hash_dog")) {
								dog.setBark("gone");
								context.replace(dog);
							} else {
								context.getVirtualDatabase().put(dog);
							}
						}
					)
			);

		scan.start().join();

		assertNull(org1.get(Cat.class, "boots"));
		var mittens = org1.get(Cat.class, "mittens");
		assertEquals(1, mittens.getRevision());
		assertEquals("gone", mittens.getPur());
		var horse = org2.get(Cat.class, "horse");
		assertEquals(2, horse.getRevision());

		assertNull(org1.get(Dog.class, "hash_otis"));
		var dog = org2.get(Dog.class, "hash_dog");
		assertEquals(1, dog.getRevision());
		assertEquals("gone", dog.getBark());
		var lassy = org2.get(Dog.class, "hash_lassy");
		assertEquals(2, lassy.getRevision());
	}

	public static class Cat extends Table {

		private String name;
		private String pur;

		public Cat(String name, String pur) {
			this.name = name;
			this.pur = pur;
			setId(name);
		}

		public String getName() {
			return name;
		}

		public String getPur() {
			return pur;
		}

		public void setPur(String pur) {
			this.pur = pur;
		}
	}

	@Hash(SimplerHasher.class)
	public static class Dog extends Table {

		private String name;
		private String bark;

		public Dog(String name, String bark) {
			this.name = name;
			this.bark = bark;
			setId(name);
		}

		public String getName() {
			return name;
		}

		public String getBark() {
			return bark;
		}

		public void setBark(String bark) {
			this.bark = bark;
		}
	}
}
