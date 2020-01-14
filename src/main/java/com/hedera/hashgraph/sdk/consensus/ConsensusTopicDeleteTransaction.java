package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.proto.ConsensusDeleteTopicTransactionBody;
import com.hedera.hashgraph.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.TransactionBuilder;

import io.grpc.MethodDescriptor;

public class ConsensusTopicDeleteTransaction extends TransactionBuilder<ConsensusTopicDeleteTransaction> {
    private final ConsensusDeleteTopicTransactionBody.Builder builder = bodyBuilder.getConsensusDeleteTopicBuilder();

    public ConsensusTopicDeleteTransaction() {
        super();
    }

    public ConsensusTopicDeleteTransaction setTopicId(ConsensusTopicId topicId) {
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
