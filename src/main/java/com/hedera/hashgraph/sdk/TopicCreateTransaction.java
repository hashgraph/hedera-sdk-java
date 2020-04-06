package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ConsensusCreateTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class TopicCreateTransaction extends TransactionBuilder<TopicCreateTransaction> {
    private final ConsensusCreateTopicTransactionBody.Builder builder;

    public TopicCreateTransaction() {
        builder = ConsensusCreateTopicTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusCreateTopic(builder);
    }
}
