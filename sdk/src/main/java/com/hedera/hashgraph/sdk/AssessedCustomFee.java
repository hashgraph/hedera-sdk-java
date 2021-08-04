package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;

public class AssessedCustomFee {
    public final long amount;
    @Nullable
    public final TokenId tokenId;
    @Nullable
    public final AccountId feeCollectorAccountId;

    private AssessedCustomFee(long amount, @Nullable TokenId tokenId, @Nullable AccountId feeCollectorAccountId) {
        this.amount = amount;
        this.tokenId = tokenId;
        this.feeCollectorAccountId = feeCollectorAccountId;
    }

    static AssessedCustomFee fromProtobuf(com.hedera.hashgraph.sdk.proto.AssessedCustomFee assessedCustomFee) {
        return new AssessedCustomFee(
            assessedCustomFee.getAmount(),
            assessedCustomFee.hasTokenId() ? TokenId.fromProtobuf(assessedCustomFee.getTokenId()) : null,
            assessedCustomFee.hasFeeCollectorAccountId() ? AccountId.fromProtobuf(assessedCustomFee.getFeeCollectorAccountId()) : null
        );
    }

    public static AssessedCustomFee fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.AssessedCustomFee.parseFrom(bytes).toBuilder().build());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("amount", amount)
            .add("tokenId", tokenId)
            .add("feeCollectorAccountId", feeCollectorAccountId)
            .toString();
    }

    com.hedera.hashgraph.sdk.proto.AssessedCustomFee toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.AssessedCustomFee.newBuilder().setAmount(amount);
        if(tokenId != null) {
            builder.setTokenId(tokenId.toProtobuf());
        }
        if(feeCollectorAccountId != null) {
            builder.setFeeCollectorAccountId(feeCollectorAccountId.toProtobuf());
        }
        return builder.build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
