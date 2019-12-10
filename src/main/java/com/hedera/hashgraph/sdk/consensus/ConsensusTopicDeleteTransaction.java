package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hederahashgraph.api.proto.java.ConsensusUpdateTopicTransactionBody;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.service.proto.java.ConsensusServiceGrpc;

import io.grpc.MethodDescriptor;

public class ConsensusTopicDeleteTransaction extends TransactionBuilder<ConsensusTopicDeleteTransaction> {
    private final ConsensusUpdateTopicTransactionBody.Builder builder = bodyBuilder.getConsensusUpdateTopicBuilder();

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
