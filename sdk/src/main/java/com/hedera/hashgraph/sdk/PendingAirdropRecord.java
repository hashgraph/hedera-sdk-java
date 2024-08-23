package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.PendingAirdropValue;

public class PendingAirdropRecord {
    private final PendingAirdropId pendingAirdropId;
    private final long pendingAirdropAmount;

    public PendingAirdropRecord(PendingAirdropId pendingAirdropId, long pendingAirdropAmount) {
        this.pendingAirdropId = pendingAirdropId;
        this.pendingAirdropAmount = pendingAirdropAmount;
    }


    public com.hedera.hashgraph.sdk.proto.PendingAirdropRecord toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.PendingAirdropRecord.newBuilder()
            .setPendingAirdropId(this.pendingAirdropId.toProtobuf())
            .setPendingAirdropValue(PendingAirdropValue.newBuilder().setAmount(pendingAirdropAmount))
            .build();
    }

    static PendingAirdropRecord fromProtobuf(com.hedera.hashgraph.sdk.proto.PendingAirdropRecord pendingAirdropRecord) {
        return new PendingAirdropRecord(
            PendingAirdropId.fromProtobuf(pendingAirdropRecord.getPendingAirdropId()),
            pendingAirdropRecord.getPendingAirdropValue().getAmount());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("pendingAirdropId", pendingAirdropId)
            .add("pendingAirdropAmount",  pendingAirdropAmount)
            .toString();
    }
}
