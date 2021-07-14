package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.Fraction;
import com.hedera.hashgraph.sdk.proto.FractionalFee;

public class CustomFractionalFee extends CustomFee {
    private long numerator = 0;
    private long denominator = 1;
    private long min = 0;
    private long max = 0;

    public CustomFractionalFee() {
    }

    static CustomFractionalFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        var fractionalFee = customFee.getFractionalFee();
        var fraction = fractionalFee.getFractionalAmount();
        var returnFee = new CustomFractionalFee()
            .setNumerator(fraction.getNumerator())
            .setDenominator(fraction.getDenominator())
            .setMin(fractionalFee.getMinimumAmount())
            .setMax(fractionalFee.getMaximumAmount());
        if(customFee.hasFeeCollectorAccountId()) {
            returnFee.setFeeCollectorAccountId(AccountId.fromProtobuf(customFee.getFeeCollectorAccountId()));
        }
        return returnFee;
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
                FractionalFee.newBuilder()
                    .setMinimumAmount(getMin())
                    .setMaximumAmount(getMax())
                    .setFractionalAmount(
                        Fraction.newBuilder()
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
