package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ConsensusSubmitMessageTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

public final class MessageSubmitTransaction extends TransactionBuilder<MessageSubmitTransaction> {
    private final ConsensusSubmitMessageTransactionBody.Builder builder;

    public MessageSubmitTransaction() {
        builder = ConsensusSubmitMessageTransactionBody.newBuilder();
    }

    @Override
    protected void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusSubmitMessage(builder);
    }
}
