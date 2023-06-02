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

public class CustomFeeListTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    private static List<CustomFee> spawnCustomFeeListExample() {
        var returnList = new ArrayList<CustomFee>();
        returnList.add(new CustomFixedFee()
            .setFeeCollectorAccountId(new AccountId(4322))
            .setDenominatingTokenId(new TokenId(483902))
            .setAmount(10)
        );
        returnList.add(new CustomFractionalFee()
            .setFeeCollectorAccountId(new AccountId(389042))
            .setNumerator(3)
            .setDenominator(7)
            .setMin(3)
            .setMax(100)
        );
        returnList.add(new CustomRoyaltyFee()
            .setFeeCollectorAccountId(new AccountId(23423))
            .setNumerator(5)
            .setDenominator(8)
            .setFallbackFee(new CustomFixedFee()
                .setDenominatingTokenId(new TokenId(483902))
                .setAmount(10))
        );
        return returnList;
    }

    @Test
    void shouldSerialize() throws Exception {
        var originalCustomFeeList = spawnCustomFeeListExample();
        byte[] customFee0Bytes = originalCustomFeeList.get(0).toBytes();
        byte[] customFee1Bytes = originalCustomFeeList.get(1).toBytes();
        byte[] customFee2Bytes = originalCustomFeeList.get(2).toBytes();
        var copyCustomFeeList = new ArrayList<CustomFee>();
        copyCustomFeeList.add(CustomFee.fromBytes(customFee0Bytes));
        copyCustomFeeList.add(CustomFee.fromBytes(customFee1Bytes));
        copyCustomFeeList.add(CustomFee.fromBytes(customFee2Bytes));
        assertThat(originalCustomFeeList.toString()).isEqualTo(copyCustomFeeList.toString());
        SnapshotMatcher.expect(originalCustomFeeList.toString()).toMatchSnapshot();
    }

    @Test
    void deepClone() throws Exception {
        var originalCustomFeeList = spawnCustomFeeListExample();
        var copyCustomFeeList = new ArrayList<CustomFee>();
        for (var fee : originalCustomFeeList) {
            copyCustomFeeList.add(fee.deepClone());
        }
        var originalCustomFeeListString = originalCustomFeeList.toString();
        assertThat(originalCustomFeeListString).isEqualTo(copyCustomFeeList.toString());

        // modifying clone doesn't affect original
        ((CustomFixedFee) copyCustomFeeList.get(0)).setDenominatingTokenId(new TokenId(89803));
        assertThat(originalCustomFeeListString).isEqualTo(originalCustomFeeList.toString());

        SnapshotMatcher.expect(originalCustomFeeList.toString()).toMatchSnapshot();
    }
}
