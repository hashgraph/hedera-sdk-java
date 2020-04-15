package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ConsensusDeleteTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class TopicDeleteTransaction extends TransactionBuilder<TopicDeleteTransaction> {
    private final ConsensusDeleteTopicTransactionBody.Builder builder;

    public TopicDeleteTransaction() {
        builder = ConsensusDeleteTopicTransactionBody.newBuilder();
    }

    /**
     * Set the topic ID to delete.
     *
     * @return {@code this}.
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
