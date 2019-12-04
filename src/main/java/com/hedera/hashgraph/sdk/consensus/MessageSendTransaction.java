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

public class MessageSendTransaction extends TransactionBuilder<MessageSendTransaction> {
    private ConsensusSubmitMessageTransactionBody.Builder builder = bodyBuilder.getConsensusSubmitMessageBuilder();

    public MessageSendTransaction(@Nullable Client client) {
        super(client);
    }

    public MessageSendTransaction setTopic(TopicId topic) {
        builder.setTopicID(topic.toProto());
        return this;
    }

    public MessageSendTransaction setMessage(byte[] message) {
        builder.setMessage(ByteString.copyFrom(message));
        return this;
    }

    public MessageSendTransaction setMessage(String message) {
        builder.setMessage(ByteString.copyFromUtf8(message));
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasTopicID(), "setTopic() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getSubmitMessageMethod();
    }
}
