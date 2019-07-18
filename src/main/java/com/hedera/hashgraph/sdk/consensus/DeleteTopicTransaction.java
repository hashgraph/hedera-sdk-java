package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.proto.ConsensusDeleteTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
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
        require(builder.hasTopicID(), ".setTopicId() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getDeleteTopicMethod();
    }
}
