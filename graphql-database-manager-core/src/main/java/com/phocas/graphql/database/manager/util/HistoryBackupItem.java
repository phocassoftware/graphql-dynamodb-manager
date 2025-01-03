package com.fleetpin.graphql.database.manager.util;

import java.nio.ByteBuffer;

public interface HistoryBackupItem extends BackupItem {
	String getOrganisationIdType();

	byte[] getIdRevision();

	byte[] getIdDate();

	byte[] getStartsWithUpdatedAt();

	Long getUpdatedAt();
}
