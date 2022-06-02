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

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenFeeScheduleUpdateTransactionTest {
    final Instant validStart = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    private TokenFeeScheduleUpdateTransaction spawnTestTransaction() {
        var customFees = new ArrayList<CustomFee>();
        customFees.add(new CustomFixedFee()
            .setFeeCollectorAccountId(new AccountId(4322))
            .setDenominatingTokenId(new TokenId(483902))
            .setAmount(10)
        );
        customFees.add(new CustomFractionalFee()
            .setFeeCollectorAccountId(new AccountId(389042))
            .setNumerator(3)
            .setDenominator(7)
            .setMin(3)
            .setMax(100)
            .setAssessmentMethod(FeeAssessmentMethod.EXCLUSIVE)
        );

        return new TokenFeeScheduleUpdateTransaction()
            .setTokenId(new TokenId(8798))
            .setCustomFees(customFees)
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .freeze();
    }

    @Test
    void shouldSerialize() {
        assertDoesNotThrow(() -> {
            var originalUpdate = spawnTestTransaction();
            byte[] updateBytes = originalUpdate.toBytes();
            var copyUpdate = TokenFeeScheduleUpdateTransaction.fromBytes(updateBytes);
            assertTrue(originalUpdate.toString().equals(copyUpdate.toString()));
            SnapshotMatcher.expect(originalUpdate.toString()).toMatchSnapshot();
        });
    }
}
