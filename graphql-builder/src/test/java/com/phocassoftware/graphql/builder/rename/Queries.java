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
package com.phocassoftware.graphql.builder.rename;

import com.phocassoftware.graphql.builder.annotations.GraphQLName;
import com.phocassoftware.graphql.builder.annotations.Query;

public class Queries {

	@Query
	@GraphQLName("passthroughClass")
	public static ClassType passthroughClassWrong(@GraphQLName("type") ClassType typeWrong) {
		return typeWrong;
	}

	@Query
	@GraphQLName("passthroughRecord")
	public static RecordType passthroughRecordWrong(@GraphQLName("type") RecordType typeWrong) {
		return typeWrong;
	}

	public static record RecordType(@GraphQLName("name") String nameWrong) {}

	public static class ClassType {

		private String nameWrong;

		@GraphQLName("nameGet")
		public String getNameWrong() {
			return nameWrong;
		}

		@GraphQLName("nameSet")
		public void setNameWrong(String nameWrong) {
			this.nameWrong = nameWrong;
		}
	}
}
