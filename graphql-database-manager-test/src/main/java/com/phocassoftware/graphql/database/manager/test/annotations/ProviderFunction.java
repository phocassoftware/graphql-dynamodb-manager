package com.phocassoftware.graphql.database.manager.test.annotations;

import com.phocassoftware.graphql.database.manager.test.ArgumentProvider;

public interface ProviderFunction<T> {

	Class<T> type();

	T create(ArgumentProvider provider);

}
