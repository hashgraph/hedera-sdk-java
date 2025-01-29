// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.ByteString;
import io.github.jsonSnapshot.SnapshotMatcher;
import java.time.Instant;
import java.util.Arrays;
import org.hiero.sdk.java.proto.ConsensusSubmitMessageTransactionBody;
import org.hiero.sdk.java.proto.SchedulableTransactionBody;
import org.hiero.sdk.java.proto.TransactionBody;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TopicMessageSubmitTransactionTest {
    private static final PrivateKey unusedPrivateKey = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");
    private static final TopicId testTopicId = new TopicId(0, 6, 9);
    private static final byte[] testMessageBytes = {0x04, 0x05, 0x06};
    private static final Instant validStart = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    private TopicMessageSubmitTransaction spawnTestTransactionString() {
        return new TopicMessageSubmitTransaction()
                .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
                .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
                .setTopicId(testTopicId)
                .setMessage(new String(testMessageBytes))
                .freeze()
                .sign(unusedPrivateKey);
    }

    private TopicMessageSubmitTransaction spawnTestTransactionBytes() {
        return new TopicMessageSubmitTransaction()
                .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
                .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
                .setTopicId(testTopicId)
                .setMessage(testMessageBytes)
                .freeze()
                .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytesNoSetters() throws Exception {
        var tx = new TopicMessageSubmitTransaction();
        var tx2 = Transaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void fromScheduledTransaction() {
        var transactionBody = SchedulableTransactionBody.newBuilder()
                .setConsensusSubmitMessage(
                        ConsensusSubmitMessageTransactionBody.newBuilder().build())
                .build();

        var tx = Transaction.fromScheduledTransaction(transactionBody);

        assertThat(tx).isInstanceOf(TopicMessageSubmitTransaction.class);
    }

    @Test
    void constructTopicMessageSubmitTransactionFromTransactionBodyProtobuf() {
        var transactionBody = ConsensusSubmitMessageTransactionBody.newBuilder()
                .setTopicID(testTopicId.toProtobuf())
                .setMessage(ByteString.copyFrom(testMessageBytes))
                .build();

        var tx = TransactionBody.newBuilder()
                .setConsensusSubmitMessage(transactionBody)
                .build();

        var topicSubmitMessageTransaction = new TopicMessageSubmitTransaction(tx);
        assertThat(topicSubmitMessageTransaction.getTopicId()).isEqualTo(testTopicId);
    }

    @Test
    void getSetTopicId() {
        var topicSubmitMessageTransaction = new TopicMessageSubmitTransaction().setTopicId(testTopicId);
        assertThat(topicSubmitMessageTransaction.getTopicId()).isEqualTo(testTopicId);
    }

    @Test
    void getSetTopicIdFrozen() {
        var tx = spawnTestTransactionString();
        assertThrows(IllegalStateException.class, () -> tx.setTopicId(testTopicId));
    }

    @Test
    void getSetMessage() {
        var topicSubmitMessageTransactionString =
                new TopicMessageSubmitTransaction().setMessage(new String(testMessageBytes));
        var topicSubmitMessageTransactionBytes = new TopicMessageSubmitTransaction().setMessage(testMessageBytes);
        assertThat(topicSubmitMessageTransactionString.getMessage().toByteArray())
                .isEqualTo(testMessageBytes);
        assertThat(topicSubmitMessageTransactionBytes.getMessage().toByteArray())
                .isEqualTo(testMessageBytes);
    }

    @Test
    void getSetMessageFrozen() {
        var topicSubmitMessageTransactionString = spawnTestTransactionString();
        var topicSubmitMessageTransactionBytes = spawnTestTransactionBytes();
        assertThrows(
                IllegalStateException.class, () -> topicSubmitMessageTransactionString.setMessage(testMessageBytes));
        assertThrows(
                IllegalStateException.class, () -> topicSubmitMessageTransactionBytes.setMessage(testMessageBytes));
    }
}
