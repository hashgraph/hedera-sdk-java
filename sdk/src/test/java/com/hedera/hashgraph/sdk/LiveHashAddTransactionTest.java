package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class LiveHashAddTransactionTest {
    private static final PrivateKey privateKey = PrivateKey.fromString(
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

    private LiveHashAddTransaction spawnTestTransaction() {
        return new LiveHashAddTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setAccountId(AccountId.fromString("0.0.100"))
            .setHash(ByteString.copyFrom("hash", StandardCharsets.UTF_8))
            .setKeys(privateKey)
            .setDuration(Duration.ofDays(30))
            .freeze()
            .sign(privateKey);
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = LiveHashAddTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }
}
