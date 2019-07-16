package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.proto.*;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;

public final class DeleteTopicTransaction extends TransactionBuilder<DeleteTopicTransaction> {
    private final ConsensusDeleteTopicTransactionBody.Builder builder = bodyBuilder.getConsensusDeleteTopicBuilder();

    public DeleteTopicTransaction(@Nullable Client client) {
        super(client);
    }

    public DeleteTopicTransaction setTopicId(TopicId topicId) {
        builder.setTopicID(topicId.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getDeleteTopicMethod();
    }
}
