package com.hedera.hashgraph.sdk.token;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.proto.Fraction;
import com.hedera.hashgraph.proto.FractionalFee;
import com.hedera.hashgraph.sdk.account.AccountId;

@Beta
public class CustomFractionalFee extends CustomFee {
    private long numerator = 0, denominator = 1, min = 0, max = 0;

    public CustomFractionalFee() {
    }

    CustomFractionalFee(com.hedera.hashgraph.proto.CustomFee customFee) {
        FractionalFee fractionalFee = customFee.getFractionalFee();
        Fraction fraction = fractionalFee.getFractionalAmount();

        this.feeCollectorAccountId = customFee.hasFeeCollectorAccountId()
            ? new AccountId(customFee.getFeeCollectorAccountId())
            : null;
        this.numerator = fraction.getNumerator();
        this.denominator = fraction.getDenominator();
        this.min = fractionalFee.getMinimumAmount();
        this.max = fractionalFee.getMaximumAmount();
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
    com.hedera.hashgraph.proto.CustomFee toProto() {
        com.hedera.hashgraph.proto.CustomFee.Builder customFeeBuilder = com.hedera.hashgraph.proto.CustomFee.newBuilder()
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
        if (getFeeCollectorAccountId() != null) {
            customFeeBuilder.setFeeCollectorAccountId(getFeeCollectorAccountId().toProto());
        }
        return customFeeBuilder.build();
    }
}
