package com.fleetpin.graphql.database.manager.util;

import java.nio.ByteBuffer;

public interface HistoryBackupItem extends BackupItem {
	String getOrganisationIdType();

	ByteBuffer getIdRevision();

	ByteBuffer getIdDate();

	ByteBuffer getStartsWithUpdatedAt();

	Long getUpdatedAt();
}
