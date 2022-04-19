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

public class CustomFractionalFee extends CustomFee {
    private long numerator = 0;
    private long denominator = 1;
    private long min = 0;
    private long max = 0;
    private FeeAssessmentMethod assessmentMethod = FeeAssessmentMethod.INCLUSIVE;

    public CustomFractionalFee() {
    }

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

    static CustomFractionalFee fromProtobuf(FractionalFee fractionalFee) {
        var fraction = fractionalFee.getFractionalAmount();
        return new CustomFractionalFee()
            .setNumerator(fraction.getNumerator())
            .setDenominator(fraction.getDenominator())
            .setMin(fractionalFee.getMinimumAmount())
            .setMax(fractionalFee.getMaximumAmount())
            .setAssessmentMethod(FeeAssessmentMethod.valueOf(fractionalFee.getNetOfTransfers()));
    }

    static CustomFractionalFee fromProtobuf(com.hedera.hashgraph.sdk.proto.CustomFee customFee) {
        var returnFee = fromProtobuf(customFee.getFractionalFee());
        if (customFee.hasFeeCollectorAccountId()) {
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

    public FeeAssessmentMethod getAssessmentMethod() {
        return assessmentMethod;
    }

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
