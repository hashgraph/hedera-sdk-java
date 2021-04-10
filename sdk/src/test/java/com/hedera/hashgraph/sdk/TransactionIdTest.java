package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.bouncycastle.util.encoders.Hex;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionIdTest {
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
        SnapshotMatcher.expect(TransactionId.fromString("0.0.23847@1588539964.632521325").toProtobuf().toString())
            .toMatchSnapshot();
    }

    @Test
    void shouldParse() {
        var transactionId = TransactionId.fromString("0.0.23847@1588539964.632521325");

        assertEquals(transactionId.accountId.shard, 0);
        assertEquals(transactionId.accountId.num, 23847);
        assertEquals(transactionId.validStart.getEpochSecond(), 1588539964);
        assertEquals(transactionId.validStart.getNano(), 632521325);
    }

    @Test
    void shouldParseScheduled() {
        var transactionId = TransactionId.fromString("0.0.23847@1588539964.632521325?scheduled");

        assertEquals(transactionId.accountId.shard, 0);
        assertEquals(transactionId.accountId.num, 23847);
        assertEquals(transactionId.validStart.getEpochSecond(), 1588539964);
        assertEquals(transactionId.validStart.getNano(), 632521325);
        assertTrue(transactionId.scheduled);

        assertEquals(transactionId.toString(), "0.0.23847@1588539964.632521325?scheduled");
    }
}
