package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.FixedFee;

import javax.annotation.Nullable;

public class CustomFixedFee extends CustomFee {
    private long amount = 0;
    @Nullable
    private TokenId denominatingTokenId = null;

    public CustomFixedFee() {
    }

    static CustomFixedFee clonedFrom(CustomFixedFee source) {
        var returnFee = new CustomFixedFee();
        returnFee.amount = source.amount;
        returnFee.denominatingTokenId = source.denominatingTokenId;
        returnFee.feeCollectorAccountId = source.feeCollectorAccountId;
        return returnFee;
    }

    static CustomFixedFee fromProtobuf(FixedFee fixedFee) {
        var returnFee = new CustomFixedFee()
            .setAmount(fixedFee.getAmount());
        if (fixedFee.hasDenominatingTokenId()) {
            returnFee.setDenominatingTokenId(TokenId.fromProtobuf(fixedFee.getDenominatingTokenId()));
        }
        return returnFee;
    }

    static CustomFixedFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        var returnFee = fromProtobuf(customFee.getFixedFee());
        if (customFee.hasFeeCollectorAccountId()) {
            returnFee.setFeeCollectorAccountId(AccountId.fromProtobuf(customFee.getFeeCollectorAccountId()));
        }
        return returnFee;
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

    public Hbar getHbarAmount() {
        return Hbar.fromTinybars(amount);
    }

    public CustomFixedFee setHbarAmount(Hbar amount) {
        denominatingTokenId = null;
        this.amount = amount.toTinybars();
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

    public CustomFixedFee setDenominatingTokenToSameToken() {
        denominatingTokenId = new TokenId(0, 0, 0);
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        super.validateChecksums(client);
        if (denominatingTokenId != null) {
            denominatingTokenId.validateChecksum(client);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("feeCollectorAccountId", getFeeCollectorAccountId())
            .add("amount", getAmount())
            .add("demoninatingTokenId", getDenominatingTokenId())
            .toString();
    }

    FixedFee toFixedFeeProtobuf() {
        var fixedFeeBuilder = FixedFee.newBuilder()
            .setAmount(getAmount());
        if (getDenominatingTokenId() != null) {
            fixedFeeBuilder.setDenominatingTokenId(getDenominatingTokenId().toProtobuf());
        }
        return fixedFeeBuilder.build();
    }

    @Override
    com.hedera.hashgraph.sdk.proto.CustomFee toProtobuf() {
        var customFeeBuilder = com.hedera.hashgraph.sdk.proto.CustomFee.newBuilder()
            .setFixedFee(toFixedFeeProtobuf());
        return finishToProtobuf(customFeeBuilder);
    }
}
