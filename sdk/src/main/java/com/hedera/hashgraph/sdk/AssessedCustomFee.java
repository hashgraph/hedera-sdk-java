package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A custom transfer fee that was assessed during the handling of a CryptoTransfer.
 */
public class AssessedCustomFee {
    public final long amount;
    @Nullable
    public final TokenId tokenId;
    @Nullable
    public final AccountId feeCollectorAccountId;
    public final List<AccountId> payerAccountIdList;

    private AssessedCustomFee(
        long amount,
        @Nullable TokenId tokenId,
        @Nullable AccountId feeCollectorAccountId,
        List<AccountId> payerAccountIdList
    ) {
        this.amount = amount;
        this.tokenId = tokenId;
        this.feeCollectorAccountId = feeCollectorAccountId;
        this.payerAccountIdList = payerAccountIdList;
    }

    /**
     * Convert the protobuf object to an assessed custom fee object.
     *
     * @param assessedCustomFee         protobuf response object
     * @return                          the converted assessed custom fee object
     */
    static AssessedCustomFee fromProtobuf(com.hedera.hashgraph.sdk.proto.AssessedCustomFee assessedCustomFee) {
        var payerList = new ArrayList<AccountId>(assessedCustomFee.getEffectivePayerAccountIdCount());
        for (var payerId : assessedCustomFee.getEffectivePayerAccountIdList()) {
            payerList.add(AccountId.fromProtobuf(payerId));
        }
        return new AssessedCustomFee(
            assessedCustomFee.getAmount(),
            assessedCustomFee.hasTokenId() ? TokenId.fromProtobuf(assessedCustomFee.getTokenId()) : null,
            assessedCustomFee.hasFeeCollectorAccountId() ? AccountId.fromProtobuf(assessedCustomFee.getFeeCollectorAccountId()) : null,
            payerList
        );
    }

    /**
     * Convert a byte array into an assessed custom fee object.
     *
     * @param bytes                     the byte array
     * @return                          the converted assessed custom fee object
     * @throws InvalidProtocolBufferException
     */
    public static AssessedCustomFee fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.AssessedCustomFee.parseFrom(bytes).toBuilder().build());
    }

    /**
     * @return                          string representation of assessed custom fee object
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("amount", amount)
            .add("tokenId", tokenId)
            .add("feeCollectorAccountId", feeCollectorAccountId)
            .add("payerAccountIdList", payerAccountIdList)
            .toString();
    }

    /**
     * @return {@code {@link com.hedera.hashgraph.sdk.proto.AssessedCustomFee}}
     */
    com.hedera.hashgraph.sdk.proto.AssessedCustomFee toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.AssessedCustomFee.newBuilder().setAmount(amount);
        if (tokenId != null) {
            builder.setTokenId(tokenId.toProtobuf());
        }
        if (feeCollectorAccountId != null) {
            builder.setFeeCollectorAccountId(feeCollectorAccountId.toProtobuf());
        }
        for (var payerId : payerAccountIdList) {
            builder.addEffectivePayerAccountId(payerId.toProtobuf());
        }
        return builder.build();
    }

    /**
     * @return                          the converted assessed custom fees
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
