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

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoTransferTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionList;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class CryptoTransferTransactionTest {
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
    void shouldSerialize() {
        SnapshotMatcher.expect(spawnTestTransaction()
            .toString()
        ).toMatchSnapshot();
    }

    private TransferTransaction spawnTestTransaction() {
        return new TransferTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .addHbarTransfer(AccountId.fromString("0.0.5008"), Hbar.fromTinybars(400))
            .addHbarTransfer(AccountId.fromString("0.0.5006"), Hbar.fromTinybars(800).negated())
            .addHbarTransfer(AccountId.fromString("0.0.5007"), Hbar.fromTinybars(400))
            .addTokenTransfer(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.5008"), 400)
            .addTokenTransferWithDecimals(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.5006"), -800, 3)
            .addTokenTransferWithDecimals(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.5007"), 400, 3)
            .addTokenTransfer(TokenId.fromString("0.0.4"), AccountId.fromString("0.0.5008"), 1)
            .addTokenTransfer(TokenId.fromString("0.0.4"), AccountId.fromString("0.0.5006"), -1)
            .addNftTransfer(TokenId.fromString("0.0.3").nft(2), AccountId.fromString("0.0.5008"), AccountId.fromString("0.0.5007"))
            .addNftTransfer(TokenId.fromString("0.0.3").nft(1), AccountId.fromString("0.0.5008"), AccountId.fromString("0.0.5007"))
            .addNftTransfer(TokenId.fromString("0.0.3").nft(3), AccountId.fromString("0.0.5008"), AccountId.fromString("0.0.5006"))
            .addNftTransfer(TokenId.fromString("0.0.3").nft(4), AccountId.fromString("0.0.5007"), AccountId.fromString("0.0.5006"))
            .addNftTransfer(TokenId.fromString("0.0.2").nft(4), AccountId.fromString("0.0.5007"), AccountId.fromString("0.0.5006"))
            .setHbarTransferApproval(AccountId.fromString("0.0.5007"), true)
            .setTokenTransferApproval(TokenId.fromString("0.0.4"), AccountId.fromString("0.0.5006"), true)
            .setNftTransferApproval(new NftId(TokenId.fromString("0.0.4"), 4), true)
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .freeze()
            .sign(unusedPrivateKey);
    }

    private TransferTransaction spawnModifiedTestTransaction() {
        return new TransferTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .addHbarTransfer(AccountId.fromString("0.0.5008"), Hbar.fromTinybars(400))
            .addHbarTransfer(AccountId.fromString("0.0.5006"), Hbar.fromTinybars(800).negated())
            .addHbarTransfer(AccountId.fromString("0.0.5007"), Hbar.fromTinybars(400))
            .addTokenTransfer(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.5008"), 400)
            .addTokenTransferWithDecimals(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.5006"), -800, 3)
            .addTokenTransferWithDecimals(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.5007"), 400, 3)
            .addTokenTransfer(TokenId.fromString("0.0.4"), AccountId.fromString("0.0.5008"), 1)
            .addTokenTransfer(TokenId.fromString("0.0.4"), AccountId.fromString("0.0.5006"), -1)
            .addNftTransfer(TokenId.fromString("0.0.3").nft(2), AccountId.fromString("0.0.5008"), AccountId.fromString("0.0.5007"))
            .addNftTransfer(TokenId.fromString("0.0.3").nft(1), AccountId.fromString("0.0.5008"), AccountId.fromString("0.0.5007"))
            .addNftTransfer(TokenId.fromString("0.0.3").nft(3), AccountId.fromString("0.0.5008"), AccountId.fromString("0.0.5006"))
            .addNftTransfer(TokenId.fromString("0.0.3").nft(4), AccountId.fromString("0.0.5007"), AccountId.fromString("0.0.5006"))
            .addNftTransfer(TokenId.fromString("0.0.2").nft(4), AccountId.fromString("0.0.5007"), AccountId.fromString("0.0.5006"))
            .setHbarTransferApproval(AccountId.fromString("0.0.5007"), true)
            // !!! .setTokenTransferApproval(TokenId.fromString("0.0.4"), AccountId.fromString("0.0.5006"), true) !!!
            .setNftTransferApproval(new NftId(TokenId.fromString("0.0.4"), 4), true)
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = TransferTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void decimalsMustBeConsistent() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            new TransferTransaction()
                .addTokenTransferWithDecimals(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.8"), 100, 2)
                .addTokenTransferWithDecimals(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.7"), -100, 3);
        });
    }

    @Test
    void canGetDecimals() {
        var tx = new TransferTransaction();
        assertThat(tx.getTokenIdDecimals().get(TokenId.fromString("0.0.5"))).isNull();
        tx.addTokenTransfer(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.8"), 100);
        assertThat(tx.getTokenIdDecimals().get(TokenId.fromString("0.0.5"))).isNull();
        tx.addTokenTransferWithDecimals(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.7"), -100, 5);
        assertThat(tx.getTokenIdDecimals().get(TokenId.fromString("0.0.5"))).isEqualTo(5);
    }

    @Test
    void transactionBodiesMustMatch() throws InvalidProtocolBufferException {
        com.hedera.hashgraph.sdk.proto.Transaction tx1 = TransactionList.parseFrom(spawnTestTransaction().toBytes())
            .getTransactionList(0);
        com.hedera.hashgraph.sdk.proto.Transaction tx2 = TransactionList.parseFrom(spawnModifiedTestTransaction().toBytes())
            .getTransactionList(1);
        var brokenTxList = TransactionList.newBuilder()
            .addTransactionList(tx1)
            .addTransactionList(tx2);
        var brokenTxBytes = brokenTxList.build().toByteArray();
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            Transaction.fromBytes(brokenTxBytes);
        });
    }

    @Test
    void fromScheduledTransaction() {
        var transactionBody = SchedulableTransactionBody.newBuilder()
            .setCryptoTransfer(CryptoTransferTransactionBody.newBuilder().build())
            .build();

        var tx = Transaction.fromScheduledTransaction(transactionBody);

        assertThat(tx).isInstanceOf(TransferTransaction.class);
    }
}
