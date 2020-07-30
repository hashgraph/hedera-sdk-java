package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ConsensusMessageChunkInfo;
import com.hedera.hashgraph.sdk.proto.ConsensusSubmitMessageTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Submit a message for consensus.
 * <p>
 * Valid and authorized messages on valid topics will be ordered by the consensus service, gossipped to the
 * mirror net, and published (in order) to all subscribers (from the mirror net) on this topic.
 * <p>
 * The submitKey (if any) must sign this transaction.
 * <p>
 * On success, the resulting TransactionReceipt contains the topic's updated topicSequenceNumber and
 * topicRunningHash.
 */
public final class TopicMessageSubmitTransaction extends TransactionBuilder<TransactionId, TransactionList, TopicMessageSubmitTransaction> {
    private static final int CHUNK_SIZE = 4096;

    @Nullable
    private TopicId topicId;

    @Nullable
    private ConsensusMessageChunkInfo chunkInfo;

    private ByteString message = ByteString.EMPTY;

    private int maxChunks = 10;

    public TopicMessageSubmitTransaction() {
        super();
    }

    /**
     * Set the topic ID to submit the message to.
     *
     * @return {@code this}
     * @param topicId The TopicId to be set
     */
    public TopicMessageSubmitTransaction setTopicId(TopicId topicId) {
        this.topicId = topicId;
        return this;
    }

    /**
     * Set the message to submit, as a UTF-8 string.
     *
     * @return {@code this}
     * @param message The String to be set as message
     */
    public TopicMessageSubmitTransaction setMessage(String message) {
        this.message = ByteString.copyFromUtf8(message);
        return this;
    }

    /**
     * Set the message to submit, as a byte array.
     *
     * @return {@code this}
     * @param message The array of bytes to be set as message
     */
    public TopicMessageSubmitTransaction setMessage(byte[] message) {
        this.message = ByteString.copyFrom(message);
        return this;
    }

    public TopicMessageSubmitTransaction setChunkInfo(TransactionId initialTransactionId, int total, int number) {
        this.chunkInfo = ConsensusMessageChunkInfo.newBuilder()
            .setInitialTransactionID(initialTransactionId.toProtobuf())
            .setTotal(total)
            .setNumber(number)
            .build();
        return this;
    }

    public TopicMessageSubmitTransaction setMaxChunks(int maxChunks) {
        this.maxChunks = maxChunks;
        return this;
    }

    @Override
    public TransactionList build(@Nullable Client client) {
        if (chunkInfo != null) {
            System.out.println("Here");
            SingleMessageSubmitTransaction singleTransaction = new SingleMessageSubmitTransaction(
                bodyBuilder.buildPartial(),
                topicId,
                chunkInfo,
                message);

            return new TransactionList(Collections.singleton(singleTransaction.build(client)));
        }

        // lock into a transaction ID
        TransactionId initialTransactionId;
        if (!bodyBuilder.hasTransactionID()) {
            if (client == null || client.getOperatorId() == null) {
                throw new IllegalStateException("client must have an operator or set a transaction ID to build a consensus message transaction");
            }

            initialTransactionId = TransactionId.generate(client.getOperatorId());
        } else {
            initialTransactionId = TransactionId.fromProtobuf(bodyBuilder.getTransactionID());
        }

        long totalMessageSize = this.message.size();
        long requiredChunks = (totalMessageSize + (CHUNK_SIZE - 1)) / CHUNK_SIZE;

        if (requiredChunks > maxChunks) {
            throw new IllegalArgumentException(
                "message of " + totalMessageSize + " bytes requires " + requiredChunks
                    + " chunks but the maximum allowed chunks is " + maxChunks + ", try using setMaxChunks");
        }

        ArrayList<com.hedera.hashgraph.sdk.Transaction> transactions = new ArrayList<>();
        @Var TransactionId nextTransactionId = initialTransactionId;

        for (int i = 0; i < requiredChunks; i += 1) {
            @Var int startIndex = i * CHUNK_SIZE;
            @Var int endIndex = startIndex + CHUNK_SIZE;

            if (endIndex > totalMessageSize) {
                endIndex = (int) totalMessageSize;
            }

            ByteString chunkMessage = message.substring(startIndex, endIndex);

            bodyBuilder.setTransactionID(nextTransactionId.toProtobuf());

            transactions.add(new SingleMessageSubmitTransaction(
                bodyBuilder.buildPartial(),
                topicId,
                ConsensusMessageChunkInfo.newBuilder()
                    .setInitialTransactionID(initialTransactionId.toProtobuf())
                    .setTotal((int) requiredChunks)
                    .setNumber(i + 1) // 1..=total
                    .build(),
                chunkMessage).build(client));

            // add 1 ns to make cascading transaction IDs
            nextTransactionId = new TransactionId(nextTransactionId.accountId, nextTransactionId.validStart.plusNanos(1));
        }

        return new TransactionList(transactions);
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {}

    static class SingleMessageSubmitTransaction extends SingleTransactionBuilder<SingleMessageSubmitTransaction> {
        SingleMessageSubmitTransaction(
            TransactionBody bodyBuilder,
            @Nullable TopicId topicId,
            ConsensusMessageChunkInfo chunkInfo,
            ByteString message)
        {
            this.bodyBuilder.mergeFrom(bodyBuilder);

            @SuppressWarnings("ModifiedButNotUsed")
            var builder = this.bodyBuilder.getConsensusSubmitMessage().toBuilder();

            if (topicId != null) {
                builder.setTopicID(topicId.toProtobuf());
            }

            builder.setChunkInfo(chunkInfo);
            builder.setMessage(message);

            this.bodyBuilder.setConsensusSubmitMessage(builder.build());
        }

        @Override
        void onBuild(TransactionBody.Builder bodyBuilder) {
        }
    }
}
