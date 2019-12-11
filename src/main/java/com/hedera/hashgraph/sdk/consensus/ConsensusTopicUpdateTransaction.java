package com.hedera.hashgraph.sdk.consensus;

import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.proto.ConsensusUpdateTopicTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.proto.ConsensusServiceGrpc;

import java.time.Instant;

import io.grpc.MethodDescriptor;

public class ConsensusTopicUpdateTransaction extends TransactionBuilder<ConsensusTopicUpdateTransaction> {
    private final ConsensusUpdateTopicTransactionBody.Builder builder = bodyBuilder.getConsensusUpdateTopicBuilder();

    public ConsensusTopicUpdateTransaction() {
        super();
    }

    public ConsensusTopicUpdateTransaction setTopicId(ConsensusTopicId topicId) {
        builder.setTopicID(topicId.toProto());
        return this;
    }

    /**
     * @see ConsensusTopicCreateTransaction#setTopicMemo(String)
     */
    public ConsensusTopicUpdateTransaction setTopicMemo(String topicMemo) {
        builder.setMemo(StringValue.of(topicMemo));
        return this;
    }

    /**
     * @see ConsensusTopicCreateTransaction#setAdminKey(PublicKey)
     */
    public ConsensusTopicUpdateTransaction setAdminKey(PublicKey key) {
        builder.setAdminKey(key.toKeyProto());
        return this;
    }

    /**
     * @see ConsensusTopicCreateTransaction#setSubmitKey(PublicKey)
     */
    public ConsensusTopicUpdateTransaction setSubmitKey(PublicKey key) {
        builder.setSubmitKey(key.toKeyProto());
        return this;
    }

    /**
     * @see ConsensusTopicCreateTransaction#setTopicValidStartTime(Instant)
     */
    public ConsensusTopicUpdateTransaction setTopicValidStartTime(Instant validStartTime) {
        builder.setValidStartTime(TimestampHelper.timestampFrom(validStartTime));
        return this;
    }

    /**
     * @see ConsensusTopicCreateTransaction#setTopicExpirationTime(Instant)
     */
    public ConsensusTopicUpdateTransaction setTopicExpirationTime(Instant expirationTime) {
        builder.setExpirationTime(TimestampHelper.timestampFrom(expirationTime));
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
