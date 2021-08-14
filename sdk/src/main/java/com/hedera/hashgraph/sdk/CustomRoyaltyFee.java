package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.Fraction;
import com.hedera.hashgraph.sdk.proto.RoyaltyFee;

import javax.annotation.Nullable;
import java.util.Objects;

public class CustomRoyaltyFee extends CustomFee {
    private long numerator = 0;
    private long denominator = 1;
    @Nullable
    private CustomFixedFee fallbackFee = null;

    public CustomRoyaltyFee() {
    }

    static CustomRoyaltyFee clonedFrom(CustomRoyaltyFee source) {
        var returnFee = new CustomRoyaltyFee();
        returnFee.numerator = source.numerator;
        returnFee.denominator = source.denominator;
        returnFee.fallbackFee = source.fallbackFee;
        returnFee.feeCollectorAccountId = source.feeCollectorAccountId;
        return returnFee;
    }

    static CustomRoyaltyFee fromProtobuf(RoyaltyFee royaltyFee) {
        var fraction = royaltyFee.getExchangeValueFraction();
        var returnFee = new CustomRoyaltyFee()
            .setNumerator(fraction.getNumerator())
            .setDenominator(fraction.getDenominator());
        if(royaltyFee.hasFallbackFee()) {
            returnFee.fallbackFee = CustomFixedFee.fromProtobuf(royaltyFee.getFallbackFee());
        }
        return returnFee;
    }

    static CustomRoyaltyFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        var returnFee = fromProtobuf(customFee.getRoyaltyFee());
        if(customFee.hasFeeCollectorAccountId()) {
            returnFee.setFeeCollectorAccountId(AccountId.fromProtobuf(customFee.getFeeCollectorAccountId()));
        }
        return returnFee;
    }

    public CustomRoyaltyFee setFeeCollectorAccountId(AccountId feeCollectorAccountId) {
        doSetFeeCollectorAccountId(feeCollectorAccountId);
        return this;
    }

    public CustomRoyaltyFee setNumerator(long numerator) {
        this.numerator = numerator;
        return this;
    }

    public long getNumerator() {
        return numerator;
    }

    public CustomRoyaltyFee setDenominator(long denominator) {
        this.denominator = denominator;
        return this;
    }

    public long getDenominator() {
        return denominator;
    }

    public CustomRoyaltyFee setFallbackFee(CustomFixedFee fallbackFee) {
        Objects.requireNonNull(fallbackFee);
        this.fallbackFee = CustomFixedFee.clonedFrom(fallbackFee);
        return this;
    }

    RoyaltyFee toRoyaltyFeeProtobuf() {
        var royaltyFeeBuilder = RoyaltyFee.newBuilder()
            .setExchangeValueFraction(
                Fraction.newBuilder()
                    .setNumerator(numerator)
                    .setDenominator(denominator)
            );
        if(fallbackFee != null) {
            royaltyFeeBuilder.setFallbackFee(fallbackFee.toFixedFeeProtobuf());
        }
        return royaltyFeeBuilder.build();
    }

    @Override
    com.hedera.hashgraph.sdk.proto.CustomFee toProtobuf() {
        var customFeeBuilder = com.hedera.hashgraph.sdk.proto.CustomFee.newBuilder()
            .setRoyaltyFee(toRoyaltyFeeProtobuf());
        return finishToProtobuf(customFeeBuilder);
    }

}
