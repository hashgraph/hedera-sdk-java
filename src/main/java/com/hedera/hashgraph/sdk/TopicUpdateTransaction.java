package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ConsensusUpdateTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class TopicUpdateTransaction extends TransactionBuilder<TopicUpdateTransaction> {
    private final ConsensusUpdateTopicTransactionBody.Builder builder;

    public TopicUpdateTransaction() {
        builder = ConsensusUpdateTopicTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusUpdateTopic(builder);
    }
}
