package com.hedera.hashgraph.sdk.consensus;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.proto.*;
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
        builder.setTopicID(TopicID.newBuilder()
            .setRealmNum(topicId.getRealmNum())
            .setShardNum(topicId.getShardNum())
            .setTopicNum(topicId.getTopicNum()));
        return this;
    }

    @Override
    protected void doValidate() {
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getCreateTopicMethod();
    }
}
