package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.Key;
import com.hedera.hashgraph.sdk.proto.*;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;

public final class CreateTopicTransaction extends TransactionBuilder<CreateTopicTransaction> {

    private final ConsensusCreateTopicTransactionBody.Builder builder = bodyBuilder.getConsensusCreateTopicBuilder();

    public CreateTopicTransaction(@Nullable Client client) {
        super(client);
    }

    @Override
    protected void doValidate() {
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getCreateTopicMethod();
    }

    public CreateTopicTransaction setShardId(long shardId) {
        builder.setShardID(ShardID.newBuilder().setShardNum(shardId));
        return this;
    }

    public CreateTopicTransaction setRealmId(long realmId) {
        builder.setRealmID(RealmID.newBuilder().setRealmNum(realmId));
        return this;
    }

    public CreateTopicTransaction setAdminKey(Key adminKey) {
        builder.setAdminKey(adminKey.toKeyProto());
        return this;
    }

    public CreateTopicTransaction setCreationTime(Instant creationTime) {
        builder.setCreationTime(TimestampHelper.timestampFrom(creationTime));
        return this;
    }

    public CreateTopicTransaction setExpirationDuration(Duration expirationDuration) {
        builder.setExpirationDuration(DurationHelper.durationFrom(expirationDuration));
        return this;
    }

    public CreateTopicTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expirationTime));
        return this;
    }

    public CreateTopicTransaction setSubmitKey(Key submitKey) {
        builder.setSubmitKey(submitKey.toKeyProto());
        return this;
    }

    public CreateTopicTransaction setTopicMemo(String memo) {
        builder.setMemo(memo);
        return this;
    }
}
