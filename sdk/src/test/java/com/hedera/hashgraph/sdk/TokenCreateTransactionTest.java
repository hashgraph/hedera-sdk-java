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

import com.hedera.hashgraph.sdk.proto.ConsensusSubmitMessageTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenCreateTransactionBody;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenCreateTransactionTest {
    private static final PrivateKey unusedPrivateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

    final Instant validStart = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void shouldSerializeFungible() {
        SnapshotMatcher.expect(spawnTestTransaction()
            .toString()
        ).toMatchSnapshot();
    }

    @Test
    void shouldSerializeNft() {
        SnapshotMatcher.expect(spawnTestTransactionNft()
            .toString()
        ).toMatchSnapshot();
    }

    private TokenCreateTransaction spawnTestTransactionNft() {
        return new TokenCreateTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setFeeScheduleKey(unusedPrivateKey)
            .setSupplyKey(unusedPrivateKey)
            .setMaxSupply(500)
            .setAdminKey(unusedPrivateKey)
            .setAutoRenewAccountId(AccountId.fromString("0.0.123"))
            .setAutoRenewPeriod(Duration.ofSeconds(100))
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setSupplyType(TokenSupplyType.FINITE)
            .setFreezeKey(unusedPrivateKey)
            .setWipeKey(unusedPrivateKey)
            .setTokenSymbol("F")
            .setKycKey(unusedPrivateKey)
            .setPauseKey(unusedPrivateKey)
            .setExpirationTime(validStart)
            .setTreasuryAccountId(AccountId.fromString("0.0.456"))
            .setTokenName("floof")
            .setTokenMemo("Floof says hi")
            .setMaxTransactionFee(new Hbar(1))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytesFungible() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = TokenCreateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    private TokenCreateTransaction spawnTestTransaction() {
        return new TokenCreateTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setInitialSupply(30)
            .setFeeScheduleKey(unusedPrivateKey)
            .setSupplyKey(unusedPrivateKey)
            .setAdminKey(unusedPrivateKey)
            .setAutoRenewAccountId(AccountId.fromString("0.0.123"))
            .setAutoRenewPeriod(Duration.ofSeconds(100))
            .setDecimals(3)
            .setFreezeDefault(true)
            .setFreezeKey(unusedPrivateKey)
            .setWipeKey(unusedPrivateKey)
            .setTokenSymbol("F")
            .setKycKey(unusedPrivateKey)
            .setPauseKey(unusedPrivateKey)
            .setExpirationTime(validStart)
            .setTreasuryAccountId(AccountId.fromString("0.0.456"))
            .setTokenName("floof")
            .setTokenMemo("Floof says hi")
            .setCustomFees(Collections.singletonList(
                new CustomFixedFee()
                    .setFeeCollectorAccountId(AccountId.fromString("0.0.543"))
                    .setAmount(3)
                    .setDenominatingTokenId(TokenId.fromString("4.3.2"))
            ))
            .setMaxTransactionFee(new Hbar(1))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytesNft() throws Exception {
        var tx = spawnTestTransactionNft();
        var tx2 = TokenCreateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void propertiesTest() {
        var tx = spawnTestTransaction();

        assertThat(tx.getTokenName()).isEqualTo("floof");
        assertThat(tx.getTokenSymbol()).isEqualTo("F");
        assertThat(tx.getDecimals()).isEqualTo(3);
        assertThat(tx.getInitialSupply()).isEqualTo(30);
        assertThat(tx.getTreasuryAccountId()).hasToString("0.0.456");
        assertThat(tx.getAdminKey()).isEqualTo(unusedPrivateKey);
        assertThat(tx.getKycKey()).isEqualTo(unusedPrivateKey);
        assertThat(tx.getFreezeKey()).isEqualTo(unusedPrivateKey);
        assertThat(tx.getWipeKey()).isEqualTo(unusedPrivateKey);
        assertThat(tx.getSupplyKey()).isEqualTo(unusedPrivateKey);
        assertThat(tx.getFeeScheduleKey()).isEqualTo(unusedPrivateKey);
        assertThat(tx.getPauseKey()).isEqualTo(unusedPrivateKey);
        assertThat(tx.getFreezeDefault()).isTrue();
        assertThat(tx.getExpirationTime()).isEqualTo(validStart);
        assertThat(tx.getAutoRenewAccountId()).hasToString("0.0.123");
        assertThat(tx.getAutoRenewPeriod()).isNull();
        assertThat(tx.getTokenMemo()).isEqualTo("Floof says hi");
        assertThat(tx.getTokenType()).isEqualTo(TokenType.FUNGIBLE_COMMON);
        assertThat(tx.getSupplyType()).isEqualTo(TokenSupplyType.INFINITE);
        assertThat(tx.getMaxSupply()).isZero();
    }

    @Test
    void fromScheduledTransaction() {
        var transactionBody = SchedulableTransactionBody.newBuilder()
            .setTokenCreation(TokenCreateTransactionBody.newBuilder().build())
            .build();

        var tx = Transaction.fromScheduledTransaction(transactionBody);

        assertThat(tx).isInstanceOf(TokenCreateTransaction.class);
    }
}
