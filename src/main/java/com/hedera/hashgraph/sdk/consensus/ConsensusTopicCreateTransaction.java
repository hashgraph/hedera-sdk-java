package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hederahashgraph.api.proto.java.ConsensusCreateTopicTransactionBody;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.service.proto.java.ConsensusServiceGrpc;

import java.time.Instant;

import io.grpc.MethodDescriptor;

public class ConsensusTopicCreateTransaction extends TransactionBuilder<ConsensusTopicCreateTransaction> {
    private final ConsensusCreateTopicTransactionBody.Builder builder = bodyBuilder.getConsensusCreateTopicBuilder();

    public ConsensusTopicCreateTransaction() {
        super();
    }

    /**
     * Short publicly visible memo about the topic. No guarantee of uniqueness.
     */
    public ConsensusTopicCreateTransaction setTopicMemo(String topicMemo) {
        builder.setMemo(topicMemo);
        return this;
    }

    /**
     * Access control for {@link ConsensusTopicUpdateTransaction} and {@link ConsensusTopicDeleteTransaction} of this topic.
     */
    public ConsensusTopicCreateTransaction setAdminKey(PublicKey key) {
        builder.setAdminKey(key.toKeyProto());
        return this;
    }

    /**
     * Access control for {@link ConsensusMessageSubmitTransaction}.
     *
     * If unspecified, no access control will be performed {@link ConsensusMessageSubmitTransaction} (all
     * submissions would be allowed).
     */
    public ConsensusTopicCreateTransaction setSubmitKey(PublicKey key) {
        builder.setSubmitKey(key.toKeyProto());
        return this;
    }

    /**
     * Effective consensus timestamp at which {@link ConsensusMessageSubmitTransaction} will begin to succeed on this topic.
     *
     * If unspecified, the consensus timestamp of this transaction will be the effective validStartTime.
     */
    public ConsensusTopicCreateTransaction setTopicValidStartTime(Instant validStartTime) {
        builder.setValidStartTime(TimestampHelper.timestampFrom(validStartTime));
        return this;
    }

    /**
     * Effective consensus timestamp at (and after) which {@link ConsensusMessageSubmitTransaction} calls will no longer
     * succeed on the topic.
     *
     * If unspecified, the consensus timestamp + 90 days will become the effective expirationTime.
     *
     * The expirationTime may be no longer than 90 days from the consensus timestamp of this transaction.
     */
    public ConsensusTopicCreateTransaction setTopicExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expirationTime));
        return this;
    }

    @Override
    protected void doValidate() {
        // No local validation needed
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getCreateTopicMethod();
    }
}
