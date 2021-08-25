package com.hedera.hashgraph.sdk.token;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.proto.FixedFee;
import com.hedera.hashgraph.proto.Fraction;
import com.hedera.hashgraph.proto.RoyaltyFee;
import com.hedera.hashgraph.sdk.account.AccountId;

import javax.annotation.Nullable;
import java.util.Objects;

public class CustomRoyaltyFee extends CustomFee {
    private long numerator = 0;
    private long denominator = 1;
    @Nullable
    private CustomFixedFee fallbackFee = null;

    public CustomRoyaltyFee() {
    }

    CustomRoyaltyFee(com.hedera.hashgraph.proto.CustomFee customFee) {
        RoyaltyFee royaltyFee = customFee.getRoyaltyFee();

        this.feeCollectorAccountId = customFee.hasFeeCollectorAccountId() ?
            new AccountId(customFee.getFeeCollectorAccountId()) : null;
        this.denominator = royaltyFee.hasExchangeValueFraction() ? royaltyFee.getExchangeValueFraction().getDenominator() : 0;
        this.numerator = royaltyFee.hasExchangeValueFraction() ? royaltyFee.getExchangeValueFraction().getNumerator() : 0;
        this.fallbackFee = royaltyFee.hasFallbackFee() ?
            new CustomFixedFee(com.hedera.hashgraph.proto.CustomFee.newBuilder()
                .setFixedFee(royaltyFee.getFallbackFee())
                .build()
            ) : null;
    }

    public CustomRoyaltyFee setFeeCollectorAccountId(AccountId feeCollectorAccountId) {
        doSetFeeCollectorAccountId(feeCollectorAccountId);
        return this;
    }

    public long getNumerator() {
        return numerator;
    }

    public CustomRoyaltyFee setNumerator(long numerator) {
        this.numerator = numerator;
        return this;
    }

    public long getDenominator() {
        return denominator;
    }

    public CustomRoyaltyFee setDenominator(long denominator) {
        this.denominator = denominator;
        return this;
    }

    public CustomRoyaltyFee setFallbackFee(CustomFixedFee fallbackFee) {
        Objects.requireNonNull(fallbackFee);
        this.fallbackFee = fallbackFee;
        return this;
    }

    @Override
    com.hedera.hashgraph.proto.CustomFee toProto() {
        RoyaltyFee.Builder builder = RoyaltyFee.newBuilder()
            .setExchangeValueFraction(
                Fraction.newBuilder()
                    .setNumerator(numerator)
                    .setDenominator(denominator)
            );

        if (fallbackFee != null) {
            builder.setFallbackFee(fallbackFee.toProto().getFixedFee());
        }

        return com.hedera.hashgraph.proto.CustomFee.newBuilder()
            .setRoyaltyFee(builder)
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("feeCollectorAccountId", getFeeCollectorAccountId())
            .add("numerator", getNumerator())
            .add("denominator", getDenominator())
            .add("fallbackFee", fallbackFee)
            .toString();
    }
}
