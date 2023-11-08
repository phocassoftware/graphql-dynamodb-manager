package com.fleetpin.graphql.database.manager.dynamo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetpin.graphql.database.manager.util.HistoryBackupItem;
import java.nio.ByteBuffer;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoHistoryBackupItem extends DynamoBackupItem implements HistoryBackupItem {

	private String organisationIdType;

	private ByteBuffer idRevision;

	private ByteBuffer idDate;

	private ByteBuffer startsWithUpdatedAt;

	private Long updatedAt;

	public DynamoHistoryBackupItem() {}

	public DynamoHistoryBackupItem(String table, Map<String, AttributeValue> item, ObjectMapper objectMapper) {
		super(table, item, objectMapper);
		this.organisationIdType = item.get("organisationIdType").s();

		this.idRevision = item.get("idRevision").b().asByteBuffer();

		this.idDate = item.get("idDate").b().asByteBuffer();

		this.startsWithUpdatedAt = item.get("startsWithUpdatedAt").b().asByteBuffer();

		this.updatedAt = Long.parseLong(item.get("updatedAt").n());
	}

	@Override
	public String getOrganisationIdType() {
		return this.organisationIdType;
	}

	@Override
	public ByteBuffer getIdRevision() {
		return this.idRevision;
	}

	@Override
	public ByteBuffer getIdDate() {
		return this.idDate;
	}

	@Override
	public ByteBuffer getStartsWithUpdatedAt() {
		return this.startsWithUpdatedAt;
	}

	@Override
	public Long getUpdatedAt() {
		return this.updatedAt;
	}
}
