package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.bouncycastle.util.encoders.Hex;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.time.Instant;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class EthereumTransactionTest {
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

    EthereumTransaction spawnTestTransaction() {
        return new EthereumTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setEthereumData(Hex.decode("deadbeef"))
            .setCallDataFileId(FileId.fromString("4.5.6"))
            .setMaxGasAllowanceHbar(Hbar.fromString("3"))
            .setMaxTransactionFee(new Hbar(1))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldSerialize() {
        SnapshotMatcher.expect(spawnTestTransaction()
            .toString()
        ).toMatchSnapshot();
    }

    @Test
    void shouldBytesNft() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = TokenCreateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx2.toString());
    }
}
