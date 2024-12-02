/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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

import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenGrantKycTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TokenGrantKycTransactionTest {
    private static final PrivateKey unusedPrivateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");
    private static final TokenId testTokenId = TokenId.fromString("4.2.0");
    private static final AccountId testAccountId = AccountId.fromString("6.9.0");

    final Instant validStart = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void shouldSerialize() {
        SnapshotMatcher.expect(spawnTestTransaction().toString()).toMatchSnapshot();
    }

    private TokenGrantKycTransaction spawnTestTransaction() {
        return new TokenGrantKycTransaction().setNodeAccountIds(
                Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setAccountId(testAccountId).setTokenId(testTokenId).setMaxTransactionFee(new Hbar(1)).freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = TokenGrantKycTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void shouldBytesNoSetters() throws Exception {
        var tx = new TokenGrantKycTransaction();
        var tx2 = Transaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void fromScheduledTransaction() {
        var transactionBody = SchedulableTransactionBody.newBuilder()
            .setTokenGrantKyc(TokenGrantKycTransactionBody.newBuilder().build()).build();

        var tx = Transaction.fromScheduledTransaction(transactionBody);

        assertThat(tx).isInstanceOf(TokenGrantKycTransaction.class);
    }

    @Test
    void constructTokenGrantKycTransactionFromTransactionBodyProtobuf() {
        var transactionBody = TokenGrantKycTransactionBody.newBuilder().setAccount(testAccountId.toProtobuf())
            .setToken(testTokenId.toProtobuf()).build();

        var tx = TransactionBody.newBuilder().setTokenGrantKyc(transactionBody).build();
        var tokenGrantKycTransaction = new TokenGrantKycTransaction(tx);

        assertThat(tokenGrantKycTransaction.getTokenId()).isEqualTo(testTokenId);
    }

    @Test
    void getSetAccountId() {
        var tokenGrantKycTransaction = new TokenGrantKycTransaction().setAccountId(testAccountId);
        assertThat(tokenGrantKycTransaction.getAccountId()).isEqualTo(testAccountId);
    }

    @Test
    void getSetAccountIdFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setAccountId(testAccountId));
    }

    @Test
    void getSetTokenId() {
        var tokenGrantKycTransaction = new TokenGrantKycTransaction().setTokenId(testTokenId);
        assertThat(tokenGrantKycTransaction.getTokenId()).isEqualTo(testTokenId);
    }

    @Test
    void getSetTokenIdFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setTokenId(testTokenId));
    }
}
