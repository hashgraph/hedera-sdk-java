package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenWipeTransactionTest {
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

    private TokenWipeTransaction spawnTestTransaction() {
        return new TokenWipeTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setTokenId(TokenId.fromString("0.0.111"))
            .setAccountId(AccountId.fromString("4.4.4"))
            .setAmount(30)
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
            .setAccountId(AccountId.fromString("4.4.4"))
            .setSerials(Collections.singletonList(444L))
            .setMaxTransactionFee(new Hbar(1))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytesFungible() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = TokenWipeTransaction.fromBytes(tx.toBytes());
        assertEquals(tx.toString(), tx2.toString());
    }

    @Test
    void shouldBytesNft() throws Exception {
        var tx = spawnTestTransactionNft();
        var tx2 = TokenWipeTransaction.fromBytes(tx.toBytes());
        assertEquals(tx.toString(), tx2.toString());
    }
}
