/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2023 Hedera Hashgraph, LLC
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

import com.hedera.hashgraph.sdk.proto.FixedFee;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomFixedFeeTest {

    private final long amount = 4;
    private final TokenId tokenId = new TokenId(5, 6, 7);

    private final FixedFee fee = FixedFee.newBuilder()
        .setAmount(amount)
        .setDenominatingTokenId(tokenId.toProtobuf())
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
        SnapshotMatcher.expect(CustomFixedFee.fromProtobuf(fee).toString()).toMatchSnapshot();
    }

    @Test
    void deepCloneSubclass() {
        var customFixedFee1 = CustomFixedFee.fromProtobuf(fee);
        var customFixedFee2 = customFixedFee1.deepCloneSubclass();

        assertThat(customFixedFee1.getFeeCollectorAccountId()).isEqualTo(customFixedFee2.getFeeCollectorAccountId());
        assertThat(customFixedFee1.getAllCollectorsAreExempt()).isEqualTo(customFixedFee2.getAllCollectorsAreExempt());
    }

    @Test
    void toProtobuf() {
        SnapshotMatcher.expect(CustomFixedFee.fromProtobuf(fee).toProtobuf().toString()).toMatchSnapshot();
    }

    @Test
    void toFixedFeeProtobuf() {
        SnapshotMatcher.expect(CustomFixedFee.fromProtobuf(fee).toFixedFeeProtobuf().toString()).toMatchSnapshot();
    }

    @Test
    void getSetAmount() {
        final var customFixedFee1 = new CustomFixedFee().setAmount(amount);
        final var customFixedFee2 = new CustomFixedFee().setHbarAmount(Hbar.fromTinybars(amount));

        assertThat(customFixedFee1.getAmount()).isEqualTo(amount);
        assertThat(customFixedFee2.getHbarAmount().toTinybars()).isEqualTo(amount);
        assertThat(customFixedFee1.getHbarAmount().toTinybars()).isEqualTo(customFixedFee2.getAmount());
    }

    @Test
    void getSetDenominatingToken() {
        final var customFixedFee = new CustomFixedFee().setDenominatingTokenId(tokenId);
        assertThat(customFixedFee.getDenominatingTokenId()).isEqualTo(tokenId);
    }

    @Test
    void setSentinelValueToken() {
        final var customFixedFee = new CustomFixedFee().setDenominatingTokenToSameToken();
        assertThat(customFixedFee.getDenominatingTokenId()).isEqualTo(new TokenId(0, 0, 0));
    }
}
