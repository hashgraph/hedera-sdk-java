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
    private final ConsensusDeleteTopicTransactionBody.Builder builder;

    @Nullable
    TopicId topicId = null;

    public TopicDeleteTransaction() {
        builder = ConsensusDeleteTopicTransactionBody.newBuilder();
    }

    TopicDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getConsensusDeleteTopic().toBuilder();

        if (builder.hasTopicID()) {
            topicId = TopicId.fromProtobuf(builder.getTopicID());
        }
    }

    TopicDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getConsensusDeleteTopic().toBuilder();

        if (builder.hasTopicID()) {
            topicId = TopicId.fromProtobuf(builder.getTopicID());
        }
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

    ConsensusDeleteTopicTransactionBody.Builder build() {
        if (topicId != null) {
            builder.setTopicID(topicId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws InvalidChecksumException {
        if (topicId != null) {
            topicId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ConsensusServiceGrpc.getDeleteTopicMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusDeleteTopic(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setConsensusDeleteTopic(build());
    }
}
