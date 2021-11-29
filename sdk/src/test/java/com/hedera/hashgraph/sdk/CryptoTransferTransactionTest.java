package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

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
        SnapshotMatcher.expect(spawnTestTransaction()
            .toString()
        ).toMatchSnapshot();
    }

    private TransferTransaction spawnTestTransaction() {
        return new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(AccountId.fromString("0.0.5005")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .addHbarTransfer(AccountId.fromString("0.0.5008"), Hbar.fromTinybars(400))
            .addHbarTransfer(AccountId.fromString("0.0.5006"), Hbar.fromTinybars(800).negated())
            .addHbarTransfer(AccountId.fromString("0.0.5007"), Hbar.fromTinybars(400))
            .addTokenTransfer(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.5008"), 400)
            .addTokenTransfer(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.5006"), -800)
            .addTokenTransfer(TokenId.fromString("0.0.5"), AccountId.fromString("0.0.5007"), 400)
            .addTokenTransfer(TokenId.fromString("0.0.4"), AccountId.fromString("0.0.5008"), 1)
            .addTokenTransfer(TokenId.fromString("0.0.4"), AccountId.fromString("0.0.5006"), -1)
            .addNftTransfer(TokenId.fromString("0.0.3").nft(2), AccountId.fromString("0.0.5008"), AccountId.fromString("0.0.5007"))
            .addNftTransfer(TokenId.fromString("0.0.3").nft(1), AccountId.fromString("0.0.5008"), AccountId.fromString("0.0.5007"))
            .addNftTransfer(TokenId.fromString("0.0.3").nft(3), AccountId.fromString("0.0.5008"), AccountId.fromString("0.0.5006"))
            .addNftTransfer(TokenId.fromString("0.0.3").nft(4), AccountId.fromString("0.0.5007"), AccountId.fromString("0.0.5006"))
            .addNftTransfer(TokenId.fromString("0.0.2").nft(4), AccountId.fromString("0.0.5007"), AccountId.fromString("0.0.5006"))
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = TransferTransaction.fromBytes(tx.toBytes());
        assertEquals(tx.toString(), tx2.toString());
    }
}
