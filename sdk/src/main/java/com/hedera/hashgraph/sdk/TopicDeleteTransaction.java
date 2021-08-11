package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ConsensusDeleteTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Delete a topic.
 * <p>
 * No more transactions or queries on the topic will succeed.
 * <p>
 * If an {@code adminKey} is set, this transaction must be signed by that key.
 * If there is no {@code adminKey}, this transaction will fail with {@link Status#UNAUTHORIZED}.
 */
public final class TopicDeleteTransaction extends Transaction<TopicDeleteTransaction> {
    @Nullable
    private TopicId topicId = null;

    public TopicDeleteTransaction() {
    }

    TopicDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TopicDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public TopicId getTopicId() {
        return topicId;
    }

    /**
     * Set the topic ID to delete.
     *
     * @return {@code this}
     * @param topicId The TopicId to be set
     */
    public TopicDeleteTransaction setTopicId(TopicId topicId) {
        Objects.requireNonNull(topicId);
        requireNotFrozen();
        this.topicId = topicId;
        return this;
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getConsensusDeleteTopic();
        if (body.hasTopicID()) {
            topicId = TopicId.fromProtobuf(body.getTopicID());
        }
    }

    ConsensusDeleteTopicTransactionBody.Builder build() {
        var builder = ConsensusDeleteTopicTransactionBody.newBuilder();
        if (topicId != null) {
            builder.setTopicID(topicId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (topicId != null) {
            topicId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ConsensusServiceGrpc.getDeleteTopicMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusDeleteTopic(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setConsensusDeleteTopic(build());
    }
}
