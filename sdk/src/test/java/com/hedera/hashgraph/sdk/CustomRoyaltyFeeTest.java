/*-
 *
 * Hedera C++ SDK
 *
 * Copyright (C) 2020 - 2023 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import com.hedera.hashgraph.sdk.proto.FixedFee;
import com.hedera.hashgraph.sdk.proto.Fraction;
import com.hedera.hashgraph.sdk.proto.RoyaltyFee;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomRoyaltyFeeTest {
    private static final boolean allCollectorsAreExempt = true;
    private static final AccountId feeCollectorAccountId = new AccountId(1, 2, 3);
    private static final int numerator = 4;
    private static final int denominator = 5;
    private static final CustomFixedFee fallbackFee = new CustomFixedFee().setAmount(6);

    private final RoyaltyFee fee = RoyaltyFee.newBuilder()
        .setExchangeValueFraction(Fraction.newBuilder().setNumerator(numerator).setDenominator(denominator))
        .setFallbackFee(FixedFee.newBuilder().setAmount(6).build())
        .build();

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void fromProtobuf() {
        SnapshotMatcher.expect(CustomRoyaltyFee.fromProtobuf(fee).toString()).toMatchSnapshot();
    }

    @Test
    void deepCloneSubclass() {
        var customRoyaltyFee = new CustomRoyaltyFee()
            .setFeeCollectorAccountId(feeCollectorAccountId)
            .setAllCollectorsAreExempt(allCollectorsAreExempt);
        var clonedCustomRoyaltyFee = customRoyaltyFee.deepCloneSubclass();

        assertThat(clonedCustomRoyaltyFee.getFeeCollectorAccountId()).isEqualTo(feeCollectorAccountId);
        assertThat(clonedCustomRoyaltyFee.getAllCollectorsAreExempt()).isEqualTo(allCollectorsAreExempt);
    }

    @Test
    void toProtobuf() {
        SnapshotMatcher.expect(CustomRoyaltyFee.fromProtobuf(fee).toProtobuf().toString()).toMatchSnapshot();
    }

    @Test
    void getSetNumerator() {
        final var customRoyaltyFee = new CustomRoyaltyFee().setNumerator(numerator);
        assertThat(customRoyaltyFee.getNumerator()).isEqualTo(numerator);
    }

    @Test
    void getSetDenominator() {
        final var customRoyaltyFee = new CustomRoyaltyFee().setDenominator(denominator);
        assertThat(customRoyaltyFee.getDenominator()).isEqualTo(denominator);
    }

    @Test
    void getSetFallbackFee() {
        final var customRoyaltyFee = new CustomRoyaltyFee().setFallbackFee(fallbackFee);
        assertThat(customRoyaltyFee.getFallbackFee()).isNotNull();
        assertThat(customRoyaltyFee.getFallbackFee().getAmount()).isEqualTo(fallbackFee.getAmount());
    }
}
