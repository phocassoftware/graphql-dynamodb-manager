package com.fleetpin.graphql.database.manager.dynamo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetpin.graphql.database.manager.util.HistoryBackupItem;
import java.nio.ByteBuffer;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoHistoryBackupItem extends DynamoBackupItem implements HistoryBackupItem {

	private String organisationIdType;

	private byte[] idRevision;

	private byte[] idDate;

	private byte[] startsWithUpdatedAt;

	private Long updatedAt;

	public DynamoHistoryBackupItem() {}

	public DynamoHistoryBackupItem(String table, Map<String, AttributeValue> item, ObjectMapper objectMapper) {
		super(table, item, objectMapper);
		this.organisationIdType = item.get("organisationIdType").s();

		this.idRevision = item.get("idRevision").b().asByteArray();

		this.idDate = item.get("idDate").b().asByteArray();

		this.startsWithUpdatedAt = item.get("startsWithUpdatedAt").b().asByteArray();

		this.updatedAt = Long.parseLong(item.get("updatedAt").n());
	}

	@Override
	public String getOrganisationIdType() {
		return this.organisationIdType;
	}

	@Override
	public byte[] getIdRevision() {
		return this.idRevision;
	}

	@Override
	public byte[] getIdDate() {
		return this.idDate;
	}

	@Override
	public byte[] getStartsWithUpdatedAt() {
		return this.startsWithUpdatedAt;
	}

	@Override
	public Long getUpdatedAt() {
		return this.updatedAt;
	}
}
