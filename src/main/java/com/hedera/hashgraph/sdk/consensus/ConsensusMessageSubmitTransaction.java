package com.hedera.hashgraph.sdk.consensus;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.proto.ConsensusSubmitMessageTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.TransactionBuilder;

import io.grpc.MethodDescriptor;

public class ConsensusMessageSubmitTransaction extends TransactionBuilder<ConsensusMessageSubmitTransaction> {
    private ConsensusSubmitMessageTransactionBody.Builder builder = bodyBuilder.getConsensusSubmitMessageBuilder();

    public ConsensusMessageSubmitTransaction() {
        super();
    }

    public ConsensusMessageSubmitTransaction setTopicId(ConsensusTopicId topicId) {
        builder.setTopicID(topicId.toProto());
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
