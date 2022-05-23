/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.Fraction;
import com.hedera.hashgraph.sdk.proto.RoyaltyFee;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Custom royalty fee utility class.
 * See <a href="https://docs.hedera.com/guides/docs/sdks/tokens/custom-token-fees#royalty-fee”>Hedera Documentation</a>
 */
public class CustomRoyaltyFee extends CustomFee {
    private long numerator = 0;
    private long denominator = 1;
    @Nullable
    private CustomFixedFee fallbackFee = null;

    /**
     * Constructor.
     */
    public CustomRoyaltyFee() {
    }

    /**
     * Clone the custom royalty fee object.
     *
     * @param source                    the source fee object
     * @return                          the new custom fee object
     */
    static CustomRoyaltyFee clonedFrom(CustomRoyaltyFee source) {
        var returnFee = new CustomRoyaltyFee();
        returnFee.numerator = source.numerator;
        returnFee.denominator = source.denominator;
        returnFee.fallbackFee = source.fallbackFee;
        returnFee.feeCollectorAccountId = source.feeCollectorAccountId;
        return returnFee;
    }

    /**
     * Create a custom royalty fee from a royalty fee protobuf.
     *
     * @param royaltyFee                the royalty fee protobuf
     * @return                          the new royalty fee object
     */
    static CustomRoyaltyFee fromProtobuf(RoyaltyFee royaltyFee) {
        var fraction = royaltyFee.getExchangeValueFraction();
        var returnFee = new CustomRoyaltyFee()
            .setNumerator(fraction.getNumerator())
            .setDenominator(fraction.getDenominator());
        if (royaltyFee.hasFallbackFee()) {
            returnFee.fallbackFee = CustomFixedFee.fromProtobuf(royaltyFee.getFallbackFee());
        }
        return returnFee;
    }

    /**
     * Create a royalty fee from a custom fee protobuf.
     *
     * @param customFee                  the custom fee protobuf
     * @return
     */
    static CustomRoyaltyFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        var returnFee = fromProtobuf(customFee.getRoyaltyFee());
        if (customFee.hasFeeCollectorAccountId()) {
            returnFee.setFeeCollectorAccountId(AccountId.fromProtobuf(customFee.getFeeCollectorAccountId()));
        }
        return returnFee;
    }

    /**
     * Assign the fee collector account id.
     *
     * @param feeCollectorAccountId     the account id of the fee collector
     * @return {@code this}
     */
    public CustomRoyaltyFee setFeeCollectorAccountId(AccountId feeCollectorAccountId) {
        doSetFeeCollectorAccountId(feeCollectorAccountId);
        return this;
    }

    /**
     * @return                          the numerator
     */
    public long getNumerator() {
        return numerator;
    }

    /**
     * Assign the numerator.
     *
     * @param numerator                 the numerator
     * @return {@code this}
     */
    public CustomRoyaltyFee setNumerator(long numerator) {
        this.numerator = numerator;
        return this;
    }

    /**
     * @return                          the denominator
     */
    public long getDenominator() {
        return denominator;
    }

    /**
     * Assign the denominator can not be zero (0).
     *
     * @param denominator               the denominator
     * @return {@code this}
     */
    public CustomRoyaltyFee setDenominator(long denominator) {
        this.denominator = denominator;
        return this;
    }

    /**
     * The fallback fee is a fixed fee that is charged to the NFT receiver
     * when there is no fungible value exchanged with the sender of the NFT.
     *
     * @param fallbackFee               the fallback fee amount
     * @return {@code this}
     */
    public CustomRoyaltyFee setFallbackFee(CustomFixedFee fallbackFee) {
        Objects.requireNonNull(fallbackFee);
        this.fallbackFee = CustomFixedFee.clonedFrom(fallbackFee);
        return this;
    }

    /**
     * Convert the royalty fee object to a protobuf.
     *
     * @return                          the protobuf object
     */
    RoyaltyFee toRoyaltyFeeProtobuf() {
        var royaltyFeeBuilder = RoyaltyFee.newBuilder()
            .setExchangeValueFraction(
                Fraction.newBuilder()
                    .setNumerator(numerator)
                    .setDenominator(denominator)
            );
        if (fallbackFee != null) {
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
