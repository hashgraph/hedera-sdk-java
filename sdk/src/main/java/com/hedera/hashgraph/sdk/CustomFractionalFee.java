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

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.Fraction;
import com.hedera.hashgraph.sdk.proto.FractionalFee;

import java.util.Objects;

/**
 * Custom fractional fee utility class.
 * See <a “https://docs.hedera.com/guides/docs/sdks/tokens/custom-token-fees#fractional-fee”>Hedera Documentation</a>
 */
public class CustomFractionalFee extends CustomFee {
    private long numerator = 0;
    private long denominator = 1;
    private long min = 0;
    private long max = 0;
    private FeeAssessmentMethod assessmentMethod = FeeAssessmentMethod.INCLUSIVE;

    /**
     * Constructor.
     */
    public CustomFractionalFee() {
    }

    /**
     * Clone a custom fractional fee object.
     *
     * @param source                    the source fee object
     * @return                          the new custom fractional fee object
     */
    static CustomFractionalFee clonedFrom(CustomFractionalFee source) {
        var returnFee = new CustomFractionalFee();
        returnFee.numerator = source.numerator;
        returnFee.denominator = source.denominator;
        returnFee.min = source.min;
        returnFee.max = source.max;
        returnFee.assessmentMethod = source.assessmentMethod;
        returnFee.feeCollectorAccountId = source.feeCollectorAccountId;
        return returnFee;
    }

    /**
     * Create a custom fractional fee from a fee protobuf.
     *
     * @param fractionalFee             the fractional fee protobuf
     * @return                           the new custom fractional fee object
     */
    static CustomFractionalFee fromProtobuf(FractionalFee fractionalFee) {
        var fraction = fractionalFee.getFractionalAmount();
        return new CustomFractionalFee()
            .setNumerator(fraction.getNumerator())
            .setDenominator(fraction.getDenominator())
            .setMin(fractionalFee.getMinimumAmount())
            .setMax(fractionalFee.getMaximumAmount())
            .setAssessmentMethod(FeeAssessmentMethod.valueOf(fractionalFee.getNetOfTransfers()));
    }

    /**
     * Create a custom fractional fee from a fixed fee protobuf.
     *
     * @param customFee                 the custom fee protobuf
     * @return                          the new custom fractional fee object
     */
    static CustomFractionalFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        var returnFee = fromProtobuf(customFee.getFractionalFee());
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
    public CustomFractionalFee setFeeCollectorAccountId(AccountId feeCollectorAccountId) {
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
    public CustomFractionalFee setNumerator(long numerator) {
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
    public CustomFractionalFee setDenominator(long denominator) {
        this.denominator = denominator;
        return this;
    }

    /**
     * @return                          the minimum fee amount
     */
    public long getMin() {
        return min;
    }

    /**
     * Assign the minimum fee amount.
     *
     * @param min                       the fee amount
     * @return {@code this}
     */
    public CustomFractionalFee setMin(long min) {
        this.min = min;
        return this;
    }

    /**
     * @return                          the fee amount
     */
    public long getMax() {
        return max;
    }

    /**
     * Assign the maximum fee amount.
     *
     * @param max                       the fee amount
     * @return
     */
    public CustomFractionalFee setMax(long max) {
        this.max = max;
        return this;
    }

    /**
     * @return                          the assessment method inclusive / exclusive
     */
    public FeeAssessmentMethod getAssessmentMethod() {
        return assessmentMethod;
    }

    /**
     * Assign the assessment method inclusive / exclusive.
     *
     * If the assessment method field is set, the token's custom fee is charged
     * to the sending account and the receiving account receives the full token
     * transfer amount. If this field is set to false, the receiver pays for
     * the token custom fees and gets the remaining token balance.
     *     INCLUSIVE(false)
     *     EXCLUSIVE(true)
     * See <a “https://docs.hedera.com/guides/docs/sdks/tokens/custom-token-fees#fractional-fee”>Hedera Documentation</a>
     *
     * @param assessmentMethod          inclusive / exclusive
     * @return {@code this}
     */
    public CustomFractionalFee setAssessmentMethod(FeeAssessmentMethod assessmentMethod) {
        Objects.requireNonNull(assessmentMethod);
        this.assessmentMethod = assessmentMethod;
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
            .add("assessmentMethod", getAssessmentMethod())
            .toString();
    }

    /**
     * Convert the fractional fee object to a protobuf.
     *
     * @return                          the protobuf object
     */
    FractionalFee toFractionalFeeProtobuf() {
        return FractionalFee.newBuilder()
            .setMinimumAmount(getMin())
            .setMaximumAmount(getMax())
            .setFractionalAmount(
                Fraction.newBuilder()
                    .setNumerator(getNumerator())
                    .setDenominator(getDenominator())
            )
            .setNetOfTransfers(assessmentMethod.code)
            .build();
    }

    @Override
    com.hedera.hashgraph.sdk.proto.CustomFee toProtobuf() {
        var customFeeBuilder = com.hedera.hashgraph.sdk.proto.CustomFee.newBuilder()
            .setFractionalFee(toFractionalFeeProtobuf());
        return finishToProtobuf(customFeeBuilder);
    }
}
