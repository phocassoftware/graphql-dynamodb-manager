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
package com.fleetpin.graphql.builder.inputgenericsRecords;

import com.fleetpin.graphql.builder.annotations.Query;
import jakarta.annotation.Nullable;
import java.util.List;

public record Change(@Nullable Wrapper<List<String>> name, @Nullable Wrapper<List<Integer>> age, @Nullable Wrapper<String> description) {
	@Query
	public static String doChange(Change input) {
		if (input.name == null) {
			return "empty";
		}
		return input.name.wrap().getFirst() + input.age.wrap() + input.description.wrap();
	}
}
