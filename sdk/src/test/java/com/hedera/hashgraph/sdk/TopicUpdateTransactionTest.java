package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

public class TopicUpdateTransactionTest {
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
    void clearShouldSerialize() {
        SnapshotMatcher.expect(new TopicUpdateTransaction()
            .setNodeAccountId(AccountId.fromString("0.0.5005"))
            .setTransactionId(new TransactionId(AccountId.fromString("0.0.5006"), validStart))
            .setTopicId(TopicId.fromString("0.0.5007"))
            .clearAdminKey()
            .clearAutoRenewAccountId(AccountId.fromString("0.0.5008"))
            .clearSubmitKey()
            .clearTopicMemo()
            .freeze()
            .sign(unusedPrivateKey)
            .toString()
        ).toMatchSnapshot();
    }

    @Test
    void setShouldSerialize() {
        SnapshotMatcher.expect(new TopicUpdateTransaction()
            .setNodeAccountId(AccountId.fromString("0.0.5005"))
            .setTransactionId(new TransactionId(AccountId.fromString("0.0.5006"), validStart))
            .setTopicId(TopicId.fromString("0.0.5007"))
            .setAdminKey(unusedPrivateKey)
            .setAutoRenewAccountId(AccountId.fromString("0.0.5009"))
            .setAutoRenewPeriod(Duration.ofHours(24))
            .setSubmitKey(unusedPrivateKey)
            .freeze()
            .sign(unusedPrivateKey)
            .toString()
        ).toMatchSnapshot();
    }
}
