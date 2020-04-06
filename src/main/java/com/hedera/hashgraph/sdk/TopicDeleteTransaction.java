package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ConsensusDeleteTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class TopicDeleteTransaction extends TransactionBuilder<TopicDeleteTransaction> {
    private final ConsensusDeleteTopicTransactionBody.Builder builder;

    public TopicDeleteTransaction() {
        builder = ConsensusDeleteTopicTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusDeleteTopic(builder);
    }
}
