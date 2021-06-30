package com.hedera.hashgraph.sdk.token;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.proto.FixedFee;
import com.hedera.hashgraph.sdk.account.AccountId;

import javax.annotation.Nullable;
import java.util.Objects;

public class CustomFixedFee extends CustomFee {
    private long amount = 0;
    @Nullable
    private TokenId denominatingTokenId = null;

    public CustomFixedFee() {
    }

    CustomFixedFee(com.hedera.hashgraph.proto.CustomFee customFee) {
        FixedFee fixedFee = customFee.getFixedFee();

        this.feeCollectorAccountId = customFee.hasFeeCollectorAccountId() ?
                new AccountId(customFee.getFeeCollectorAccountId()) : null;
        this.amount = fixedFee.getAmount();
        this.denominatingTokenId = fixedFee.hasDenominatingTokenId() ?
                new TokenId(fixedFee.getDenominatingTokenId()) : null;
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
    com.hedera.hashgraph.proto.CustomFee toProto() {
        com.hedera.hashgraph.proto.CustomFee.Builder customFeeBuilder = com.hedera.hashgraph.proto.CustomFee.newBuilder();
        FixedFee.Builder fixedFeeBuilder = FixedFee.newBuilder().setAmount(getAmount());
        if(getFeeCollectorAccountId() != null) {
            customFeeBuilder.setFeeCollectorAccountId(getFeeCollectorAccountId().toProto());
        }
        if(getDenominatingTokenId() != null) {
            fixedFeeBuilder.setDenominatingTokenId(getDenominatingTokenId().toProto());
        }
        return customFeeBuilder.setFixedFee(fixedFeeBuilder.build()).build();
    }
}
