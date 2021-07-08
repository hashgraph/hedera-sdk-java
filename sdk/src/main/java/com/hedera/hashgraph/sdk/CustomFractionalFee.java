package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;
import java.util.Objects;

public class CustomFractionalFee extends CustomFee {
    private long numerator = 0, denominator = 1, min = 0, max = 0;

    public CustomFractionalFee() {
    }

    static CustomFractionalFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        var fractionalFee = customFee.getFractionalFee();
        var fraction = fractionalFee.getFractionalAmount();
        return new CustomFractionalFee()
            .setFeeCollectorAccountId(customFee.hasFeeCollectorAccountId() ? 
                AccountId.fromProtobuf(customFee.getFeeCollectorAccountId()) : null)
            .setNumerator(fraction.getNumerator())
            .setDenominator(fraction.getDenominator())
            .setMin(fractionalFee.getMinimumAmount())
            .setMax(fractionalFee.getMaximumAmount());
    }

    public CustomFractionalFee setFeeCollectorAccountId(AccountId feeCollectorAccountId) {
        doSetFeeCollectorAccountId(feeCollectorAccountId);
        return this;
    }

    public long getNumerator() {
        return numerator;
    }

    public CustomFractionalFee setNumerator(long numerator) {
        this.numerator = numerator;
        return this;
    }

    public long getDenominator() {
        return denominator;
    }

    public CustomFractionalFee setDenominator(long denominator) {
        this.denominator = denominator;
        return this;
    }

    public long getMin() {
        return min;
    }

    public CustomFractionalFee setMin(long min) {
        this.min = min;
        return this;
    }

    public long getMax() {
        return max;
    }

    public CustomFractionalFee setMax(long max) {
        this.max = max;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("feeCollectorAccountId", getFeeCollectorAccountId())
            .add("numerator", getNumerator())
            .add("denominator", getDenominator())
            .add("min", getMin())
            .add("max", getMax())
            .toString();
    }

    @Override
    com.hedera.hashgraph.sdk.proto.CustomFee toProtobuf() {
        var customFeeBuilder = com.hedera.hashgraph.sdk.proto.CustomFee.newBuilder()
            .setFractionalFee(
                com.hedera.hashgraph.sdk.proto.FractionalFee.newBuilder()
                    .setMinimumAmount(getMin())
                    .setMaximumAmount(getMax())
                    .setFractionalAmount(
                        com.hedera.hashgraph.sdk.proto.Fraction.newBuilder()
                            .setNumerator(getNumerator())
                            .setDenominator(getDenominator())
                    )
            );
        if(getFeeCollectorAccountId() != null) {
            customFeeBuilder.setFeeCollectorAccountId(getFeeCollectorAccountId().toProtobuf());
        }
        return customFeeBuilder.build();
    }
}