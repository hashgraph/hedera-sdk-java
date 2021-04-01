package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ConsensusDeleteTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;

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

    public TopicDeleteTransaction() {
        builder = ConsensusDeleteTopicTransactionBody.newBuilder();
    }

    TopicDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getConsensusDeleteTopic().toBuilder();
    }

    TopicDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) throws InvalidProtocolBufferException {
        super(txBody);

        builder = bodyBuilder.getConsensusDeleteTopic().toBuilder();
    }

    @Nullable
    public TopicId getTopicId() {
        return builder.hasTopicID() ? TopicId.fromProtobuf(builder.getTopicID()) : null;
    }

    /**
     * Set the topic ID to delete.
     *
     * @return {@code this}
     * @param topicId The TopicId to be set
     */
    public TopicDeleteTransaction setTopicId(TopicId topicId) {
        requireNotFrozen();
        builder.setTopicID(topicId.toProtobuf());
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ConsensusServiceGrpc.getDeleteTopicMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusDeleteTopic(builder);
        return true;
    }
}
