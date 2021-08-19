package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        SnapshotMatcher.expect(new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(AccountId.fromString("0.0.5005")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .addHbarTransfer(AccountId.fromString("0.0.5006"), Hbar.fromTinybars(800).negated())
            .addHbarTransfer(AccountId.fromString("0.0.5007"), Hbar.fromTinybars(400))
            .addHbarTransfer(AccountId.fromString("0.0.5008"), Hbar.fromTinybars(400))
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .freeze()
            .sign(unusedPrivateKey)
            .toString()
        ).toMatchSnapshot();
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(AccountId.fromString("0.0.5005")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .addHbarTransfer(AccountId.fromString("0.0.5006"), Hbar.fromTinybars(800).negated())
            .addHbarTransfer(AccountId.fromString("0.0.5007"), Hbar.fromTinybars(400))
            .addHbarTransfer(AccountId.fromString("0.0.5008"), Hbar.fromTinybars(400))
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .freeze()
            .sign(unusedPrivateKey);
        var tx2 = TransferTransaction.fromBytes(tx.toBytes());
        assertEquals(tx.toString(), tx2.toString());
    }
}
