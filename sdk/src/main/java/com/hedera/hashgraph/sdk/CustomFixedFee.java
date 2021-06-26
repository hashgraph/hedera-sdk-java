package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;
import java.util.Objects;

public class CustomFixedFee extends CustomFee {
    private long amount = 0;
    @Nullable
    private TokenId denominatingTokenId = null;

    public CustomFixedFee() {
    }

    static CustomFixedFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee, @Nullable NetworkName networkName) {
        var fixedFee = customFee.getFixedFee();
        return new CustomFixedFee()
            .setFeeCollectorAccountId(customFee.hasFeeCollectorAccountId() ? 
                AccountId.fromProtobuf(customFee.getFeeCollectorAccountId(), networkName) : null)
            .setAmount(fixedFee.getAmount())
            .setDenominatingTokenId(fixedFee.hasDenominatingTokenId() ? 
                TokenId.fromProtobuf(fixedFee.getDenominatingTokenId(), networkName) : null);
    }

    public CustomFixedFee setFeeCollectorAccountId(AccountId feeCollectorAccountId) {
        doSetFeeCollectorAccountId(feeCollectorAccountId);
        return this;
    }

    public long getAmount() {
        return amount;
    }

    public CustomFixedFee setAmount(long amount) {
        this.amount = amount;
        return this;
    }

    @Nullable
    public TokenId getDenominatingTokenId() {
        return denominatingTokenId;
    }

    public CustomFixedFee setDenominatingTokenId(@Nullable TokenId tokenId) {
        denominatingTokenId = tokenId;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("feeCollectorAccountId", getFeeCollectorAccountId())
            .add("amount", getAmount())
            .add("demoninatingTokenId", getDenominatingTokenId())
            .toString();
    }

    @Override
    com.hedera.hashgraph.sdk.proto.CustomFee toProtobuf() {
        var customFeeBuilder = com.hedera.hashgraph.sdk.proto.CustomFee.newBuilder();
        var fixedFeeBuilder = com.hedera.hashgraph.sdk.proto.FixedFee.newBuilder().setAmount(getAmount());
        if(getFeeCollectorAccountId() != null) {
            customFeeBuilder.setFeeCollectorAccountId(getFeeCollectorAccountId().toProtobuf());
        }
        if(getDenominatingTokenId() != null) {
            fixedFeeBuilder.setDenominatingTokenId(getDenominatingTokenId().toProtobuf());
        }
        return customFeeBuilder.setFixedFee(fixedFeeBuilder.build()).build();
    }
}