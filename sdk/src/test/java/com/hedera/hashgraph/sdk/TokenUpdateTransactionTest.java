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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.Timestamp;
import com.hedera.hashgraph.sdk.proto.TokenUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import io.github.jsonSnapshot.SnapshotMatcher;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class TokenUpdateTransactionTest {
    private static final PrivateKey unusedPrivateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");
    private static final PublicKey testAdminKey = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e11")
        .getPublicKey();
    private static final PublicKey testKycKey = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e12")
        .getPublicKey();
    private static final PublicKey testFreezeKey = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e13")
        .getPublicKey();
    private static final PublicKey testWipeKey = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e14")
        .getPublicKey();
    private static final PublicKey testSupplyKey = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e15")
        .getPublicKey();
    private static final PublicKey testFeeScheduleKey = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e16")
        .getPublicKey();
    private static final PublicKey testPauseKey = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e17")
        .getPublicKey();
    private static final AccountId testTreasuryAccountId = AccountId.fromString("7.7.7");
    private static final AccountId testAutoRenewAccountId = AccountId.fromString("8.8.8");
    private static final String testTokenName = "test name";
    private static final String testTokenSymbol = "test symbol";
    private static final String testTokenMemo = "test memo";
    private static final TokenId testTokenId = TokenId.fromString("4.2.0");
    private static final Duration testAutoRenewPeriod = Duration.ofHours(10);
    private static final Instant testExpirationTime = Instant.now();
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
    void shouldSerialize() {
        SnapshotMatcher.expect(spawnTestTransaction().toString()).toMatchSnapshot();
    }

    private TokenUpdateTransaction spawnTestTransaction() {
        return new TokenUpdateTransaction().setNodeAccountIds(
                Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setTokenId(testTokenId).setFeeScheduleKey(testFeeScheduleKey).setSupplyKey(testSupplyKey)
            .setAdminKey(testAdminKey).setAutoRenewAccountId(testAutoRenewAccountId)
            .setAutoRenewPeriod(testAutoRenewPeriod).setFreezeKey(testFreezeKey).setWipeKey(testWipeKey)
            .setTokenSymbol(testTokenSymbol).setKycKey(testKycKey).setPauseKey(testPauseKey)
            .setExpirationTime(validStart).setTreasuryAccountId(testTreasuryAccountId).setTokenName(testTokenName)
            .setTokenMemo(testTokenMemo).setMaxTransactionFee(new Hbar(1)).freeze().sign(unusedPrivateKey);
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = TokenUpdateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void fromScheduledTransaction() {
        var transactionBody = SchedulableTransactionBody.newBuilder()
            .setTokenUpdate(TokenUpdateTransactionBody.newBuilder().build()).build();

        var tx = Transaction.fromScheduledTransaction(transactionBody);

        assertThat(tx).isInstanceOf(TokenUpdateTransaction.class);
    }

    @Test
    void constructTokenUpdateTransactionFromTransactionBodyProtobuf() {
        var transactionBody = TokenUpdateTransactionBody.newBuilder().setToken(testTokenId.toProtobuf())
            .setName(testTokenName).setSymbol(testTokenSymbol).setTreasury(testTreasuryAccountId.toProtobuf())
            .setAdminKey(testAdminKey.toProtobufKey()).setKycKey(testKycKey.toProtobufKey())
            .setFreezeKey(testFreezeKey.toProtobufKey()).setWipeKey(testWipeKey.toProtobufKey())
            .setSupplyKey(testSupplyKey.toProtobufKey()).setAutoRenewAccount(testAutoRenewAccountId.toProtobuf())
            .setAutoRenewPeriod(
                com.hedera.hashgraph.sdk.proto.Duration.newBuilder().setSeconds(testAutoRenewPeriod.toSeconds())
                    .build()).setExpiry(Timestamp.newBuilder().setSeconds(testExpirationTime.getEpochSecond()).build())
            .setMemo(StringValue.newBuilder().setValue(testTokenMemo).build())
            .setFeeScheduleKey(testFeeScheduleKey.toProtobufKey()).setPauseKey(testPauseKey.toProtobufKey()).build();

        var tx = TransactionBody.newBuilder().setTokenUpdate(transactionBody).build();
        var tokenUpdateTransaction = new TokenUpdateTransaction(tx);

        assertThat(tokenUpdateTransaction.getTokenId()).isEqualTo(testTokenId);
        assertThat(tokenUpdateTransaction.getTokenName()).isEqualTo(testTokenName);
        assertThat(tokenUpdateTransaction.getTokenSymbol()).isEqualTo(testTokenSymbol);
        assertThat(tokenUpdateTransaction.getTreasuryAccountId()).isEqualTo(testTreasuryAccountId);
        assertThat(tokenUpdateTransaction.getAdminKey()).isEqualTo(testAdminKey);
        assertThat(tokenUpdateTransaction.getKycKey()).isEqualTo(testKycKey);
        assertThat(tokenUpdateTransaction.getFreezeKey()).isEqualTo(testFreezeKey);
        assertThat(tokenUpdateTransaction.getWipeKey()).isEqualTo(testWipeKey);
        assertThat(tokenUpdateTransaction.getSupplyKey()).isEqualTo(testSupplyKey);
        assertThat(tokenUpdateTransaction.getAutoRenewAccountId()).isEqualTo(testAutoRenewAccountId);
        assertThat(tokenUpdateTransaction.getAutoRenewPeriod().toSeconds()).isEqualTo(testAutoRenewPeriod.toSeconds());
        assertThat(tokenUpdateTransaction.getExpirationTime().getEpochSecond()).isEqualTo(
            testExpirationTime.getEpochSecond());
        assertThat(tokenUpdateTransaction.getTokenMemo()).isEqualTo(testTokenMemo);
        assertThat(tokenUpdateTransaction.getFeeScheduleKey()).isEqualTo(testFeeScheduleKey);
        assertThat(tokenUpdateTransaction.getPauseKey()).isEqualTo(testPauseKey);
    }

    @Test
    void getSetTokenId() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setTokenId(testTokenId);
        assertThat(tokenUpdateTransaction.getTokenId()).isEqualTo(testTokenId);
    }

    @Test
    void getSetTokenIdFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setTokenId(testTokenId));
    }

    @Test
    void getSetName() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setTokenName(testTokenName);
        assertThat(tokenUpdateTransaction.getTokenName()).isEqualTo(testTokenName);
    }

    @Test
    void getSetNameFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setTokenName(testTokenName));
    }

    @Test
    void getSetSymbol() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setTokenSymbol(testTokenSymbol);
        assertThat(tokenUpdateTransaction.getTokenSymbol()).isEqualTo(testTokenSymbol);
    }

    @Test
    void getSetSymbolFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setTokenSymbol(testTokenSymbol));
    }

    @Test
    void getSetTreasuryAccountId() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setTreasuryAccountId(testTreasuryAccountId);
        assertThat(tokenUpdateTransaction.getTreasuryAccountId()).isEqualTo(testTreasuryAccountId);
    }

    @Test
    void getSetTreasuryAccountIdFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setTreasuryAccountId(testTreasuryAccountId));
    }

    @Test
    void getSetAdminKey() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setAdminKey(testAdminKey);
        assertThat(tokenUpdateTransaction.getAdminKey()).isEqualTo(testAdminKey);
    }

    @Test
    void getSetAdminKeyFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setAdminKey(testAdminKey));
    }

    @Test
    void getSetKycKey() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setKycKey(testKycKey);
        assertThat(tokenUpdateTransaction.getKycKey()).isEqualTo(testKycKey);
    }

    @Test
    void getSetKycKeyFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setKycKey(testKycKey));
    }

    @Test
    void getSetFreezeKey() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setFreezeKey(testFreezeKey);
        assertThat(tokenUpdateTransaction.getFreezeKey()).isEqualTo(testFreezeKey);
    }

    @Test
    void getSetFreezeKeyFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setFreezeKey(testFreezeKey));
    }

    @Test
    void getSetWipeKey() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setWipeKey(testWipeKey);
        assertThat(tokenUpdateTransaction.getWipeKey()).isEqualTo(testWipeKey);
    }

    @Test
    void getSetWipeKeyFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setWipeKey(testWipeKey));
    }

    @Test
    void getSetSupplyKey() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setSupplyKey(testSupplyKey);
        assertThat(tokenUpdateTransaction.getSupplyKey()).isEqualTo(testSupplyKey);
    }

    @Test
    void getSetSupplyKeyFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setSupplyKey(testSupplyKey));
    }

    @Test
    void getSetAutoRenewAccountId() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setAutoRenewAccountId(testAutoRenewAccountId);
        assertThat(tokenUpdateTransaction.getAutoRenewAccountId()).isEqualTo(testAutoRenewAccountId);
    }

    @Test
    void getSetAutoRenewAccountIdFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setAutoRenewAccountId(testAutoRenewAccountId));
    }

    @Test
    void getSetAutoRenewPeriod() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setAutoRenewPeriod(testAutoRenewPeriod);
        assertThat(tokenUpdateTransaction.getAutoRenewPeriod()).isEqualTo(testAutoRenewPeriod);
    }

    @Test
    void getSetAutoRenewPeriodFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setAutoRenewPeriod(testAutoRenewPeriod));
    }

    @Test
    void getSetExpirationTime() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setExpirationTime(testExpirationTime);
        assertThat(tokenUpdateTransaction.getExpirationTime()).isEqualTo(testExpirationTime);
    }

    @Test
    void getSetExpirationTimeFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setExpirationTime(testExpirationTime));
    }

    @Test
    void getSetTokenMemo() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setTokenMemo(testTokenMemo);
        assertThat(tokenUpdateTransaction.getTokenMemo()).isEqualTo(testTokenMemo);
    }

    @Test
    void getSetTokenMemoFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setTokenMemo(testTokenMemo));
    }

    @Test
    void getSetFeeScheduleKey() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setFeeScheduleKey(testFeeScheduleKey);
        assertThat(tokenUpdateTransaction.getFeeScheduleKey()).isEqualTo(testFeeScheduleKey);
    }

    @Test
    void getSetFeeScheduleKeyFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setFeeScheduleKey(testFeeScheduleKey));
    }

    @Test
    void getSetPauseKey() {
        var tokenUpdateTransaction = new TokenUpdateTransaction().setPauseKey(testPauseKey);
        assertThat(tokenUpdateTransaction.getPauseKey()).isEqualTo(testPauseKey);
    }

    @Test
    void getSetPauseKeyFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setPauseKey(testPauseKey));
    }
}
