package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.proto.*;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;

public final class CreateTopicTransaction extends TransactionBuilder<CreateTopicTransaction> {
    private final ConsensusCreateTopicTransactionBody.Builder builder = bodyBuilder.getConsensusCreateTopicBuilder();

    public CreateTopicTransaction(@Nullable Client client) {
        super(client);
    }

    @Override
    public CreateTopicTransaction setTransactionId(TransactionId transactionId) {
        if (!builder.hasShardID()) {
            setShardId(
                transactionId.getAccountId()
                    .getShardNum());
        }

        if (!builder.hasRealmID()) {
            setRealmId(
                transactionId.getAccountId()
                    .getRealmNum());
        }

        return super.setTransactionId(transactionId);
    }

    public CreateTopicTransaction setTopicMemo(String memo) {
        builder.setMemo(memo);
        return this;
    }

    public CreateTopicTransaction setShardId(long shardId) {
        builder.setShardID(
            ShardID.newBuilder()
                .setShardNum(shardId));

        return this;
    }

    public CreateTopicTransaction setRealmId(long realmId) {
        builder.setRealmID(
            RealmID.newBuilder()
                .setRealmNum(realmId));

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
