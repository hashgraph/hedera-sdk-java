package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hederahashgraph.api.proto.java.ConsensusCreateTopicTransactionBody;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.service.proto.java.ConsensusServiceGrpc;

import java.time.Instant;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

public class TopicCreateTransaction extends TransactionBuilder<TopicCreateTransaction> {
    private final ConsensusCreateTopicTransactionBody.Builder builder = bodyBuilder.getConsensusCreateTopicBuilder();

    public TopicCreateTransaction(@Nullable Client client) {
        super(client);
    }

    public TopicCreateTransaction setTopicMemo(String topicMemo) {
        builder.setMemo(topicMemo);
        return this;
    }

    public TopicCreateTransaction setAdminKey(PublicKey key) {
        builder.setAdminKey(key.toKeyProto());
        return this;
    }

    public TopicCreateTransaction setSubmitKey(PublicKey key) {
        builder.setSubmitKey(key.toKeyProto());
        return this;
    }

    public TopicCreateTransaction setTopicValidStartTime(Instant validStartTime) {
        builder.setValidStartTime(TimestampHelper.timestampFrom(validStartTime));
        return this;
    }

    public TopicCreateTransaction setTopicExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expirationTime));
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
