package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        var accountId = Objects.requireNonNull(transactionId.accountId);
        var validStart = Objects.requireNonNull(transactionId.validStart);
        assertEquals(0, accountId.shard);
        assertEquals(23847, accountId.num);
        assertEquals(1588539964, validStart.getEpochSecond());
        assertEquals(632521325, validStart.getNano());
    }

    @Test
    void shouldParseScheduled() {
        var transactionId = TransactionId.fromString("0.0.23847@1588539964.632521325?scheduled");
        var accountId = Objects.requireNonNull(transactionId.accountId);
        var validStart = Objects.requireNonNull(transactionId.validStart);
        assertEquals(0, accountId.shard);
        assertEquals(23847, accountId.num);
        assertEquals(1588539964, validStart.getEpochSecond());
        assertEquals(632521325, validStart.getNano());
        assertTrue(transactionId.scheduled);

        assertEquals(transactionId.toString(), "0.0.23847@1588539964.632521325?scheduled");
    }
}
