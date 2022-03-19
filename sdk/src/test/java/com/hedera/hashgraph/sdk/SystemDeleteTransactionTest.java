package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SystemDeleteTransactionTest {
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
    void shouldSerializeFile() {
        SnapshotMatcher.expect(spawnTestTransactionFile()
            .toString()
        ).toMatchSnapshot();
    }

    private SystemDeleteTransaction spawnTestTransactionFile() {
        return new SystemDeleteTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setFileId(FileId.fromString("0.0.444"))
            .setExpirationTime(validStart)
            .setMaxTransactionFee(new Hbar(1))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldSerializeContract() {
        SnapshotMatcher.expect(spawnTestTransactionContract()
            .toString()
        ).toMatchSnapshot();
    }

    private SystemDeleteTransaction spawnTestTransactionContract() {
        return new SystemDeleteTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setContractId(ContractId.fromString("0.0.444"))
            .setExpirationTime(validStart)
            .setMaxTransactionFee(new Hbar(1))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytesContract() throws Exception {
        var tx = spawnTestTransactionContract();
        var tx2 = ScheduleDeleteTransaction.fromBytes(tx.toBytes());
        assertEquals(tx.toString(), tx2.toString());
    }

    @Test
    void shouldBytesFile() throws Exception {
        var tx = spawnTestTransactionFile();
        var tx2 = SystemDeleteTransaction.fromBytes(tx.toBytes());
        assertEquals(tx.toString(), tx2.toString());
    }
}
