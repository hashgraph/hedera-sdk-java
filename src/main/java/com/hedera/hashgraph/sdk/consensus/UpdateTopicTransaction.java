package com.hedera.hashgraph.sdk.consensus;

import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.Key;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.ConsensusUpdateTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;

public final class UpdateTopicTransaction extends TransactionBuilder<UpdateTopicTransaction> {

    private final ConsensusUpdateTopicTransactionBody.Builder builder = bodyBuilder.getConsensusUpdateTopicBuilder();

    public UpdateTopicTransaction(@Nullable Client client) {
        super(client);
    }

    @Override
    protected void doValidate() {
        require(builder.hasTopicID(), ".setTopicId() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getUpdateTopicMethod();
    }

    public UpdateTopicTransaction setAdminKey(Key adminKey) {
        builder.setAdminKey(adminKey.toKeyProto());
        return this;
    }

    public UpdateTopicTransaction setCreationTime(Instant creationTime) {
        builder.setCreationTime(TimestampHelper.timestampFrom(creationTime));
        return this;
    }

    public UpdateTopicTransaction setExpirationDuration(Duration expirationDuration) {
        builder.setExpirationDuration(DurationHelper.durationFrom(expirationDuration));
        return this;
    }

    public UpdateTopicTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expirationTime));
        return this;
    }

    public UpdateTopicTransaction setSubmitKey(Key submitKey) {
        builder.setSubmitKey(submitKey.toKeyProto());
        return this;
    }

    public UpdateTopicTransaction setTopicId(TopicId topicId) {
        builder.setTopicID(topicId.toProto());
        return this;
    }

    public UpdateTopicTransaction setTopicMemo(String memo) {
        builder.setMemo(StringValue.of(memo));
        return this;
    }
}
