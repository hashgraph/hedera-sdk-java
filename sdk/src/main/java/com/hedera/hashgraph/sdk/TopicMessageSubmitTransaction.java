package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ConsensusMessageChunkInfo;
import com.hedera.hashgraph.sdk.proto.ConsensusSubmitMessageTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

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
public final class TopicMessageSubmitTransaction extends TransactionBuilder<TopicMessageSubmitTransaction> {
    private final ConsensusSubmitMessageTransactionBody.Builder builder;

    public TopicMessageSubmitTransaction() {
        builder = ConsensusSubmitMessageTransactionBody.newBuilder();
    }

    /**
     * Set the topic ID to submit the message to.
     *
     * @return {@code this}
     * @param topicId The TopicId to be set
     */
    public TopicMessageSubmitTransaction setTopicId(TopicId topicId) {
        builder.setTopicID(topicId.toProtobuf());
        return this;
    }

    /**
     * Set the message to submit, as a UTF-8 string.
     *
     * @return {@code this}
     * @param message The String to be set as message
     */
    public TopicMessageSubmitTransaction setMessage(String message) {
        builder.setMessage(ByteString.copyFromUtf8(message));
        return this;
    }

    /**
     * Set the message to submit, as a byte array.
     *
     * @return {@code this}
     * @param message The array of bytes to be set as message
     */
    public TopicMessageSubmitTransaction setMessage(byte[] message) {
        builder.setMessage(ByteString.copyFrom(message));
        return this;
    }

    public TopicMessageSubmitTransaction setChunkInfo(TransactionId initialTransactionId, int total, int number) {
        var chunkInfo = ConsensusMessageChunkInfo.newBuilder()
            .setInitialTransactionID(initialTransactionId.toProtobuf())
            .setTotal(total)
            .setNumber(number)
            .build();
        builder.setChunkInfo(chunkInfo);
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusSubmitMessage(builder);
    }
}
