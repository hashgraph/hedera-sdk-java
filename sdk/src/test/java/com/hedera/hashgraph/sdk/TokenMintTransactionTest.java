package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenMintTransactionTest {
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

    @Test
    void shouldSerializeMetadata() {
        SnapshotMatcher.expect(spawnMetadataTestTransaction()
            .toString()
        ).toMatchSnapshot();
    }

    private TokenMintTransaction spawnTestTransaction() {
        return new TokenMintTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setTokenId(TokenId.fromString("1.2.3"))
            .setAmount(10L)
            .setMaxTransactionFee(new Hbar(1))
            .freeze()
            .sign(unusedPrivateKey);
    }

    private TokenMintTransaction spawnMetadataTestTransaction() {
        return new TokenMintTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setTokenId(TokenId.fromString("1.2.3"))
            .setMetadata(List.of(new byte[]{1, 2, 3, 4, 5}))
            .setMaxTransactionFee(new Hbar(1))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = TokenUpdateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void shouldBytesMetadata() throws Exception {
        var tx = spawnMetadataTestTransaction();
        var tx2 = TokenUpdateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }
}
