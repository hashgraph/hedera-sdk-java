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

import com.hedera.hashgraph.sdk.proto.CryptoCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoDeleteAllowanceTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountCreateTransactionTest {
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

    AccountCreateTransaction spawnTestTransaction() {
        return new AccountCreateTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setKey(unusedPrivateKey)
            .setInitialBalance(Hbar.fromTinybars(450))
            .setProxyAccountId(AccountId.fromString("0.0.1001"))
            .setAccountMemo("some dumb memo")
            .setReceiverSignatureRequired(true)
            .setAutoRenewPeriod(Duration.ofHours(10))
            .setStakedAccountId(AccountId.fromString("0.0.3"))
            .setAlias("0x5c562e90feaf0eebd33ea75d21024f249d451417")
            .setMaxAutomaticTokenAssociations(100)
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .freeze()
            .sign(unusedPrivateKey);
    }

    AccountCreateTransaction spawnTestTransaction2() {
        return new AccountCreateTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setKey(unusedPrivateKey)
            .setInitialBalance(Hbar.fromTinybars(450))
            .setProxyAccountId(AccountId.fromString("0.0.1001"))
            .setAccountMemo("some dumb memo")
            .setReceiverSignatureRequired(true)
            .setAutoRenewPeriod(Duration.ofHours(10))
            .setStakedNodeId(4L)
            .setMaxAutomaticTokenAssociations(100)
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldSerialize() {
        SnapshotMatcher.expect(spawnTestTransaction().toString()).toMatchSnapshot();
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = AccountCreateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void shouldSerialize2() {
        SnapshotMatcher.expect(spawnTestTransaction2().toString()).toMatchSnapshot();
    }

    @Test
    void shouldBytes2() throws Exception {
        var tx = spawnTestTransaction2();
        var tx2 = AccountCreateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void propertiesTest() {
        var tx = spawnTestTransaction();

        assertThat(tx.getKey()).isEqualTo(unusedPrivateKey);
        assertThat(tx.getInitialBalance()).isEqualTo(Hbar.fromTinybars(450));
        assertThat(tx.getReceiverSignatureRequired()).isTrue();
        assertThat(tx.getProxyAccountId()).hasToString("0.0.1001");
        assertThat(tx.getAutoRenewPeriod().toHours()).isEqualTo(10);
        assertThat(tx.getMaxAutomaticTokenAssociations()).isEqualTo(100);
        assertThat(tx.getAccountMemo()).isEqualTo("some dumb memo");
        assertThat(tx.getStakedAccountId()).hasToString("0.0.3");
        assertThat(tx.getStakedNodeId()).isNull();
        assertThat(tx.getDeclineStakingReward()).isFalse();
        assertThat(tx.getAlias()).isEqualTo(EvmAddress.fromString("0x5c562e90feaf0eebd33ea75d21024f249d451417"));
    }

    @Test
    void fromScheduledTransaction() {
        var transactionBody = SchedulableTransactionBody.newBuilder()
            .setCryptoCreateAccount(CryptoCreateTransactionBody.newBuilder().build())
            .build();

        var tx = Transaction.fromScheduledTransaction(transactionBody);

        assertThat(tx).isInstanceOf(AccountCreateTransaction.class);
    }
}
