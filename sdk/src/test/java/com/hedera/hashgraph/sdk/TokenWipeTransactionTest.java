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

import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenWipeAccountTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import io.github.jsonSnapshot.SnapshotMatcher;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class TokenWipeTransactionTest {
    private static final PrivateKey unusedPrivateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");
    private static final AccountId testAccountId = AccountId.fromString("0.6.9");
    private static final TokenId testTokenId = TokenId.fromString("4.2.0");
    private static final long testAmount = 4L;
    private static final List<Long> testSerialNumbers = Arrays.asList(8L, 9L, 10L);
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

    private TokenWipeTransaction spawnTestTransaction() {
        return new TokenWipeTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setTokenId(TokenId.fromString("0.0.111"))
            .setAccountId(testAccountId)
            .setAmount(testAmount)
            .setSerials(testSerialNumbers)
            .setMaxTransactionFee(new Hbar(1))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldSerializeNft() {
        SnapshotMatcher.expect(spawnTestTransactionNft()
            .toString()
        ).toMatchSnapshot();
    }

    private TokenWipeTransaction spawnTestTransactionNft() {
        return new TokenWipeTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setTokenId(TokenId.fromString("0.0.111"))
            .setAccountId(testAccountId)
            .setSerials(Collections.singletonList(444L))
            .setMaxTransactionFee(new Hbar(1))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytesFungible() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = TokenWipeTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void shouldBytesNft() throws Exception {
        var tx = spawnTestTransactionNft();
        var tx2 = TokenWipeTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void fromScheduledTransaction() {
        var transactionBody = SchedulableTransactionBody.newBuilder()
            .setTokenWipe(TokenWipeAccountTransactionBody.newBuilder().build())
            .build();

        var tx = Transaction.fromScheduledTransaction(transactionBody);

        assertThat(tx).isInstanceOf(TokenWipeTransaction.class);
    }

    @Test
    void constructTokenWipeTransactionFromTransactionBodyProtobuf() {
        var transactionBody = TokenWipeAccountTransactionBody.newBuilder().setToken(testTokenId.toProtobuf())
            .setAccount(testAccountId.toProtobuf()).setAmount(testAmount).addAllSerialNumbers(testSerialNumbers)
            .build();

        var txBody = TransactionBody.newBuilder().setTokenWipe(transactionBody).build();
        var tokenWipeTransaction = new TokenWipeTransaction(txBody);

        assertThat(tokenWipeTransaction.getTokenId()).isEqualTo(testTokenId);
        assertThat(tokenWipeTransaction.getAccountId()).isEqualTo(testAccountId);
        assertThat(tokenWipeTransaction.getAmount()).isEqualTo(testAmount);
        assertThat(tokenWipeTransaction.getSerials()).isEqualTo(testSerialNumbers);
    }

    @Test
    void getSetTokenId() {
        var tokenWipeTransaction = new TokenWipeTransaction().setTokenId(testTokenId);
        assertThat(tokenWipeTransaction.getTokenId()).isEqualTo(testTokenId);
    }

    @Test
    void getSetTokenIdFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setTokenId(testTokenId));
    }

    @Test
    void getSetAccountId() {
        var tx = spawnTestTransaction();
        assertThat(tx.getAccountId()).isEqualTo(testAccountId);
    }

    @Test
    void getSetAccountIdFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setAccountId(testAccountId));
    }

    @Test
    void getSetAmount() {
        var tx = spawnTestTransaction();
        assertThat(tx.getAmount()).isEqualTo(testAmount);
    }

    @Test
    void getSetAmountFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setAmount(testAmount));
    }

    @Test
    void getSetSerialNumbers() {
        var tx = spawnTestTransaction();
        assertThat(tx.getSerials()).isEqualTo(testSerialNumbers);
    }

    @Test
    void getSetSerialNumbersFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setSerials(testSerialNumbers));
    }
}
