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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// TODO: update this, test deepClone()

public class TokenInfoTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    private static TokenInfo spawnTokenInfoExample() {
        List<CustomFee> fees = new ArrayList<>();
        fees.add(new CustomFixedFee()
            .setFeeCollectorAccountId(new AccountId(4322))
            .setDenominatingTokenId(new TokenId(483902))
            .setAmount(10));
        fees.add(new CustomFractionalFee()
            .setFeeCollectorAccountId(new AccountId(389042))
            .setNumerator(3)
            .setDenominator(7)
            .setMin(3)
            .setMax(100));
        return new TokenInfo(
            new TokenId(48920),
            "name",
            "&",
            3,
            1000,
            new AccountId(932),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null,
            null,
            "memo",
            fees,
            TokenType.FUNGIBLE_COMMON,
            TokenSupplyType.FINITE,
            1000000,
            null,
            true,
            LedgerId.MAINNET
        );
    }

    @Test
    void shouldSerialize() throws Exception {
        var originalTokenInfo = spawnTokenInfoExample();
        byte[] tokenInfoBytes = originalTokenInfo.toBytes();
        var copyTokenInfo = TokenInfo.fromBytes(tokenInfoBytes);
        assertThat(copyTokenInfo.toString()).isEqualTo(originalTokenInfo.toString());
        SnapshotMatcher.expect(originalTokenInfo.toString()).toMatchSnapshot();
    }
}
