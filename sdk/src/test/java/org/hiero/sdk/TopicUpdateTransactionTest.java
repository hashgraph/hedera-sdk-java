// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.StringValue;
import org.hiero.sdk.proto.ConsensusUpdateTopicTransactionBody;
import org.hiero.sdk.proto.CryptoDeleteTransactionBody;
import org.hiero.sdk.proto.SchedulableTransactionBody;
import org.hiero.sdk.proto.Timestamp;
import org.hiero.sdk.proto.TransactionBody;
import io.github.jsonSnapshot.SnapshotMatcher;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TopicUpdateTransactionTest {
    private static final PrivateKey unusedPrivateKey = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");
    private static final PublicKey testAdminKey = PrivateKey.fromString(
                    "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e11")
            .getPublicKey();
    private static final PublicKey testSubmitKey = PrivateKey.fromString(
                    "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e12")
            .getPublicKey();
    private static final TopicId testTopicId = TopicId.fromString("0.0.5007");
    private static final String testTopicMemo = "test memo";
    private static final Duration testAutoRenewPeriod = Duration.ofHours(10);
    private static final Instant testExpirationTime = Instant.now();
    private static final AccountId testAutoRenewAccountId = AccountId.fromString("8.8.8");
    private static final Instant validStart = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void clearShouldSerialize() {
        SnapshotMatcher.expect(new TopicUpdateTransaction()
                        .setNodeAccountIds(
                                Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
                        .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
                        .setTopicId(testTopicId)
                        .clearAdminKey()
                        .clearAutoRenewAccountId()
                        .clearSubmitKey()
                        .clearTopicMemo()
                        .freeze()
                        .sign(unusedPrivateKey)
                        .toString())
                .toMatchSnapshot();
    }

    @Test
    void setShouldSerialize() {
        SnapshotMatcher.expect(spawnTestTransaction().toString()).toMatchSnapshot();
    }

    @Test
    void shouldBytesNoSetters() throws Exception {
        var tx = new TopicUpdateTransaction();
        var tx2 = Transaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    private TopicUpdateTransaction spawnTestTransaction() {
        return new TopicUpdateTransaction()
                .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
                .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
                .setTopicId(testTopicId)
                .setAdminKey(testAdminKey)
                .setAutoRenewAccountId(testAutoRenewAccountId)
                .setAutoRenewPeriod(testAutoRenewPeriod)
                .setSubmitKey(testSubmitKey)
                .setTopicMemo(testTopicMemo)
                .setExpirationTime(validStart)
                .freeze()
                .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = TopicUpdateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void fromScheduledTransaction() {
        var transactionBody = SchedulableTransactionBody.newBuilder()
                .setConsensusUpdateTopic(
                        ConsensusUpdateTopicTransactionBody.newBuilder().build())
                .build();

        var tx = Transaction.fromScheduledTransaction(transactionBody);

        assertThat(tx).isInstanceOf(TopicUpdateTransaction.class);
    }

    @Test
    void constructTopicUpdateTransactionFromTransactionBodyProtobuf() {
        var transactionBody = ConsensusUpdateTopicTransactionBody.newBuilder()
                .setTopicID(testTopicId.toProtobuf())
                .setMemo(StringValue.newBuilder().setValue(testTopicMemo).build())
                .setExpirationTime(Timestamp.newBuilder()
                        .setSeconds(testExpirationTime.getEpochSecond())
                        .build())
                .setAdminKey(testAdminKey.toProtobufKey())
                .setSubmitKey(testSubmitKey.toProtobufKey())
                .setAutoRenewPeriod(org.hiero.sdk.proto.Duration.newBuilder()
                        .setSeconds(testAutoRenewPeriod.toSeconds())
                        .build())
                .setAutoRenewAccount(testAutoRenewAccountId.toProtobuf())
                .build();

        var tx = TransactionBody.newBuilder()
                .setConsensusUpdateTopic(transactionBody)
                .build();
        var topicUpdateTransaction = new TopicUpdateTransaction(tx);

        assertThat(topicUpdateTransaction.getTopicId()).isEqualTo(testTopicId);
        assertThat(topicUpdateTransaction.getTopicMemo()).isEqualTo(testTopicMemo);
        assertThat(topicUpdateTransaction.getExpirationTime().getEpochSecond())
                .isEqualTo(testExpirationTime.getEpochSecond());
        assertThat(topicUpdateTransaction.getAdminKey()).isEqualTo(testAdminKey);
        assertThat(topicUpdateTransaction.getSubmitKey()).isEqualTo(testSubmitKey);
        assertThat(topicUpdateTransaction.getAutoRenewPeriod().toSeconds()).isEqualTo(testAutoRenewPeriod.toSeconds());
        assertThat(topicUpdateTransaction.getAutoRenewAccountId()).isEqualTo(testAutoRenewAccountId);
    }

    // doesn't throw an exception as opposed to C++ sdk
    @Test
    void constructTopicUpdateTransactionFromWrongTransactionBodyProtobuf() {
        var transactionBody = CryptoDeleteTransactionBody.newBuilder().build();
        var tx = TransactionBody.newBuilder().setCryptoDelete(transactionBody).build();

        new TopicUpdateTransaction(tx);
    }

    @Test
    void getSetTopicId() {
        var topicUpdateTransaction = new TopicUpdateTransaction().setTopicId(testTopicId);
        assertThat(topicUpdateTransaction.getTopicId()).isEqualTo(testTopicId);
    }

    @Test
    void getSetTopicIdFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setTopicId(testTopicId));
    }

    @Test
    void getSetTopicMemo() {
        var topicUpdateTransaction = new TopicUpdateTransaction().setTopicMemo(testTopicMemo);
        assertThat(topicUpdateTransaction.getTopicMemo()).isEqualTo(testTopicMemo);
    }

    @Test
    void getSetTopicMemoFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setTopicMemo(testTopicMemo));
    }

    @Test
    void clearTopicMemo() {
        var topicUpdateTransaction = new TopicUpdateTransaction().setTopicMemo(testTopicMemo);
        topicUpdateTransaction.clearTopicMemo();
        assertThat(topicUpdateTransaction.getTopicMemo()).isEmpty();
    }

    @Test
    void clearTopicMemoFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.clearTopicMemo());
    }

    @Test
    void getSetExpirationTime() {
        var topicUpdateTransaction = new TopicUpdateTransaction().setExpirationTime(testExpirationTime);
        assertThat(topicUpdateTransaction.getExpirationTime()).isEqualTo(testExpirationTime);
    }

    @Test
    void getSetExpirationTimeFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setExpirationTime(testExpirationTime));
    }

    @Test
    void getSetAdminKey() {
        var topicUpdateTransaction = new TopicUpdateTransaction().setAdminKey(testAdminKey);
        assertThat(topicUpdateTransaction.getAdminKey()).isEqualTo(testAdminKey);
    }

    @Test
    void getSetAdminKeyFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setAdminKey(testAdminKey));
    }

    @Test
    void clearAdminKey() {
        var topicUpdateTransaction = new TopicUpdateTransaction().setAdminKey(testAdminKey);
        topicUpdateTransaction.clearAdminKey();
        assertThat(topicUpdateTransaction.getAdminKey()).isEqualTo(new KeyList());
    }

    @Test
    void clearAdminKeyFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.clearAdminKey());
    }

    @Test
    void getSetSubmitKey() {
        var topicUpdateTransaction = new TopicUpdateTransaction().setSubmitKey(testSubmitKey);
        assertThat(topicUpdateTransaction.getSubmitKey()).isEqualTo(testSubmitKey);
    }

    @Test
    void getSetSubmitKeyFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setSubmitKey(testSubmitKey));
    }

    @Test
    void clearSubmitKey() {
        var topicUpdateTransaction = new TopicUpdateTransaction().setSubmitKey(testSubmitKey);
        topicUpdateTransaction.clearSubmitKey();
        assertThat(topicUpdateTransaction.getSubmitKey()).isEqualTo(new KeyList());
    }

    @Test
    void clearSubmitKeyFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.clearSubmitKey());
    }

    @Test
    void getSetAutoRenewPeriod() {
        var topicUpdateTransaction = new TopicUpdateTransaction().setAutoRenewPeriod(testAutoRenewPeriod);
        assertThat(topicUpdateTransaction.getAutoRenewPeriod()).isEqualTo(testAutoRenewPeriod);
    }

    @Test
    void getSetAutoRenewPeriodFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setAutoRenewPeriod(testAutoRenewPeriod));
    }

    @Test
    void getSetAutoRenewAccountId() {
        var topicUpdateTransaction = new TopicUpdateTransaction().setAutoRenewAccountId(testAutoRenewAccountId);
        assertThat(topicUpdateTransaction.getAutoRenewAccountId()).isEqualTo(testAutoRenewAccountId);
    }

    @Test
    void getSetAutoRenewAccountIdFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setAutoRenewAccountId(testAutoRenewAccountId));
    }

    @Test
    void clearAutoRenewAccountId() {
        var topicUpdateTransaction = new TopicUpdateTransaction().setAutoRenewAccountId(testAutoRenewAccountId);
        topicUpdateTransaction.clearAutoRenewAccountId();
        assertThat(topicUpdateTransaction.getAutoRenewAccountId()).isEqualTo(new AccountId(0));
    }

    @Test
    void clearAutoRenewAccountIdFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.clearAutoRenewAccountId());
    }
}
