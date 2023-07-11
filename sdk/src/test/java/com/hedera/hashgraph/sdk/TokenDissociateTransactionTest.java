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
import com.hedera.hashgraph.sdk.proto.TokenDissociateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import io.github.jsonSnapshot.SnapshotMatcher;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TokenDissociateTransactionTest {
    private static final PrivateKey unusedPrivateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");
    private static final AccountId testAccountId = AccountId.fromString("6.9.0");

    private static final List<TokenId> testTokenIds = Arrays.asList(TokenId.fromString("4.2.0"),
        TokenId.fromString("4.2.1"), TokenId.fromString("4.2.2"));

    final Instant validStart = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void shouldSerialize() {
        SnapshotMatcher.expect(spawnTestTransaction().toString()).toMatchSnapshot();
    }

    private TokenDissociateTransaction spawnTestTransaction() {
        return new TokenDissociateTransaction().setNodeAccountIds(
                Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setAccountId(testAccountId).setTokenIds(testTokenIds).setMaxTransactionFee(new Hbar(1)).freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = TokenDissociateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void fromScheduledTransaction() {
        var transactionBody = SchedulableTransactionBody.newBuilder()
            .setTokenDissociate(TokenDissociateTransactionBody.newBuilder().build()).build();

        var tx = Transaction.fromScheduledTransaction(transactionBody);

        assertThat(tx).isInstanceOf(TokenDissociateTransaction.class);
    }

    @Test
    void constructTokenDissociateTransactionFromTransactionBodyProtobuf() {
        var transactionBody = TokenDissociateTransactionBody.newBuilder().setAccount(testAccountId.toProtobuf())
            .addAllTokens(testTokenIds.stream().map(TokenId::toProtobuf).toList()).build();

        var tx = TransactionBody.newBuilder().setTokenDissociate(transactionBody).build();
        var tokenDissociateTransaction = new TokenDissociateTransaction(tx);

        assertThat(tokenDissociateTransaction.getAccountId()).isEqualTo(testAccountId);
        assertThat(tokenDissociateTransaction.getTokenIds().size()).isEqualTo(testTokenIds.size());
    }

    @Test
    void getSetAccountId() {
        var tokenDissociateTransaction = new TokenDissociateTransaction().setAccountId(testAccountId);
        assertThat(tokenDissociateTransaction.getAccountId()).isEqualTo(testAccountId);
    }

    @Test
    void getSetAccountIdFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setAccountId(testAccountId));
    }

    @Test
    void getSetTokenIds() {
        var tokenDissociateTransaction = new TokenDissociateTransaction().setTokenIds(testTokenIds);
        assertThat(tokenDissociateTransaction.getTokenIds()).isEqualTo(testTokenIds);
    }

    @Test
    void getSetTokenIdsFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setTokenIds(testTokenIds));
    }
}
