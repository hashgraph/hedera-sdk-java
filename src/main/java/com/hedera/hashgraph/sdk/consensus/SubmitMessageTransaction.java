package com.hedera.hashgraph.sdk.consensus;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.ConsensusSubmitMessageTransactionBody;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;

public final class SubmitMessageTransaction extends TransactionBuilder<SubmitMessageTransaction> {

    private final ConsensusSubmitMessageTransactionBody.Builder builder = bodyBuilder.getConsensusSubmitMessageBuilder();

    public SubmitMessageTransaction(@Nullable Client client) {
        super(client);
    }

    public SubmitMessageTransaction setMessage(byte[] message) {
        builder.setMessage(ByteString.copyFrom(message));
        return this;
    }

    public SubmitMessageTransaction setTopicId(TopicId topicId) {
        builder.setTopicID(topicId.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasTopicID(), ".setTopicId() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getSubmitMessageMethod();
    }
}
