package com.hedera.hashgraph.sdk.consensus;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hederahashgraph.api.proto.java.ConsensusSubmitMessageTransactionBody;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.service.proto.java.ConsensusServiceGrpc;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

public class ConsensusMessageSubmitTransaction extends TransactionBuilder<ConsensusMessageSubmitTransaction> {
    private ConsensusSubmitMessageTransactionBody.Builder builder = bodyBuilder.getConsensusSubmitMessageBuilder();

    public ConsensusMessageSubmitTransaction() {
        super();
    }

    public ConsensusMessageSubmitTransaction setTopicId(TopicId topic) {
        builder.setTopicID(topic.toProto());
        return this;
    }

    public ConsensusMessageSubmitTransaction setMessage(byte[] message) {
        builder.setMessage(ByteString.copyFrom(message));
        return this;
    }

    public ConsensusMessageSubmitTransaction setMessage(String message) {
        builder.setMessage(ByteString.copyFromUtf8(message));
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasTopicID(), "setTopicId() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getSubmitMessageMethod();
    }
}
