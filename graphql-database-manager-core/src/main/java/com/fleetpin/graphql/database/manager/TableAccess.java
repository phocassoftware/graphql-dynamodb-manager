package com.fleetpin.graphql.database.manager;

import java.util.Map;
import java.util.Set;

public interface TableAccess {
	public static <T extends Table> void setTableSource(
		final T table,
		final String sourceTable,
		final Map<String, Set<String>> links,
		final String sourceOrganisationId
	) {
		table.setSource(sourceTable, links, sourceOrganisationId);
	}

	public static <T extends Table> String getTableSourceOrganisation(final T table) {
		return table.getSourceOrganisationId();
	}

	public static <T extends Table> Map<String, Set<String>> getTableLinks(final T table) {
		return table.getLinks();
	}
}
