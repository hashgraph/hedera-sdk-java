package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ConsensusDeleteTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

/**
 * Delete a topic.
 * <p>
 * No more transactions or queries on the topic will succeed.
 * <p>
 * If an {@code adminKey} is set, this transaction must be signed by that key.
 * If there is no {@code adminKey}, this transaction will fail with {@link Status#UNAUTHORIZED}.
 */
public final class TopicDeleteTransaction extends TransactionBuilder<TopicDeleteTransaction> {
    private final ConsensusDeleteTopicTransactionBody.Builder builder;

    public TopicDeleteTransaction() {
        builder = ConsensusDeleteTopicTransactionBody.newBuilder();
    }

    /**
     * Set the topic ID to delete.
     *
     * @return {@code this}
     * @param topicId The TopicId to be set
     */
    public TopicDeleteTransaction setTopicId(TopicId topicId) {
        builder.setTopicID(topicId.toProtobuf());
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusDeleteTopic(builder);
    }
}
