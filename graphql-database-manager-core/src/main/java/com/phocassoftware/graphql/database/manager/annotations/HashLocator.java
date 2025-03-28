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

package com.phocassoftware.graphql.database.manager.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.phocassoftware.graphql.database.manager.Table;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

/**
 * used for backup destroy operations. For hashed types they need to be
 * identified from a parent object
 *
 * @author ashley.taylor
 *
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface HashLocator {
	Class<? extends HashQueryBuilder> value();

	public interface HashQueryBuilder {
		public List<HashQuery> extractHashQueries(String id);
	}

	public class HashQuery {

		private final Class<? extends Table> type;
		private final String hashId;

		public HashQuery(Class<? extends Table> type, String hashId) {
			super();
			this.type = type;
			this.hashId = hashId;
		}

		public Class<? extends Table> getType() {
			return type;
		}

		public String getHashId() {
			return hashId;
		}
	}
}
