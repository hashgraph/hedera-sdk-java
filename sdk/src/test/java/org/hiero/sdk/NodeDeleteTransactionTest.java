// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.hiero.sdk.proto.NodeDeleteTransactionBody;
import org.hiero.sdk.proto.SchedulableTransactionBody;
import org.hiero.sdk.proto.TransactionBody;
import io.github.jsonSnapshot.SnapshotMatcher;
import java.time.Instant;
import java.util.Arrays;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NodeDeleteTransactionTest {

    private static final PrivateKey TEST_PRIVATE_KEY = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

    private static final long TEST_NODE_ID = 420;

    final Instant TEST_VALID_START = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void shouldSerialize() {
        SnapshotMatcher.expect(spawnTestTransaction().toString()).toMatchSnapshot();
    }

    private NodeDeleteTransaction spawnTestTransaction() {
        return new NodeDeleteTransaction()
                .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
                .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), TEST_VALID_START))
                .setNodeId(TEST_NODE_ID)
                .setMaxTransactionFee(new Hbar(1))
                .freeze()
                .sign(TEST_PRIVATE_KEY);
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = NodeDeleteTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void shouldBytesNoSetters() throws Exception {
        var tx = new NodeDeleteTransaction();
        var tx2 = Transaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void fromScheduledTransaction() {
        var transactionBody = SchedulableTransactionBody.newBuilder()
                .setNodeDelete(NodeDeleteTransactionBody.newBuilder().build())
                .build();

        var tx = Transaction.fromScheduledTransaction(transactionBody);

        assertThat(tx).isInstanceOf(NodeDeleteTransaction.class);
    }

    @Test
    void constructNodeDeleteTransactionFromTransactionBodyProtobuf() {
        var transactionBodyBuilder = NodeDeleteTransactionBody.newBuilder();

        transactionBodyBuilder.setNodeId(TEST_NODE_ID);

        var tx = TransactionBody.newBuilder()
                .setNodeDelete(transactionBodyBuilder.build())
                .build();
        var nodeDeleteTransaction = new NodeDeleteTransaction(tx);

        assertThat(nodeDeleteTransaction.getNodeId()).isEqualTo(TEST_NODE_ID);
    }

    @Test
    void getSetNodeId() {
        var nodeDeleteTransaction = new NodeDeleteTransaction().setNodeId(TEST_NODE_ID);
        assertThat(nodeDeleteTransaction.getNodeId()).isEqualTo(TEST_NODE_ID);
    }

    @Test
    void getSetNodeIdFrozen() {
        var tx = spawnTestTransaction();
        assertThrows(IllegalStateException.class, () -> tx.setNodeId(TEST_NODE_ID));
    }
}
