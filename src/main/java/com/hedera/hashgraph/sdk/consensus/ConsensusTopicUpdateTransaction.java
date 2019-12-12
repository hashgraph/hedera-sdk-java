package com.hedera.hashgraph.sdk.consensus;

import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.Experimental;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.proto.ConsensusUpdateTopicTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.proto.ConsensusServiceGrpc;

import java.time.Duration;
import java.time.Instant;

import io.grpc.MethodDescriptor;

public class ConsensusTopicUpdateTransaction extends TransactionBuilder<ConsensusTopicUpdateTransaction> {
    private final ConsensusUpdateTopicTransactionBody.Builder builder = bodyBuilder.getConsensusUpdateTopicBuilder();

    public ConsensusTopicUpdateTransaction() {
        super();

        Experimental.requireFor(ConsensusTopicUpdateTransaction.class.getName());
    }

    public ConsensusTopicUpdateTransaction setTopicId(ConsensusTopicId topicId) {
        builder.setTopicID(topicId.toProto());
        return this;
    }

    public ConsensusTopicUpdateTransaction setTopicMemo(String topicMemo) {
        builder.setMemo(StringValue.of(topicMemo));
        return this;
    }

    public ConsensusTopicUpdateTransaction setAdminKey(PublicKey key) {
        builder.setAdminKey(key.toKeyProto());
        return this;
    }

    public ConsensusTopicUpdateTransaction setSubmitKey(PublicKey key) {
        builder.setSubmitKey(key.toKeyProto());
        return this;
    }

    public ConsensusTopicUpdateTransaction setExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expirationTime));
        return this;
    }

    public ConsensusTopicUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationHelper.durationFrom(autoRenewPeriod));
        return this;
    }

    public ConsensusTopicUpdateTransaction setAutoRenewAccountId(AccountId autoRenewAccountId) {
        builder.setAutoRenewAccount(autoRenewAccountId.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasTopicID(), ".setTopicId() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getUpdateTopicMethod();
    }
}
