package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.AccountID;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.ConsensusUpdateTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.KeyList;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.time.Duration;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Update a topic.
 * <p>
 * If there is no adminKey, the only authorized update (available to anyone) is to extend the expirationTime.
 * Otherwise transaction must be signed by the adminKey.
 * <p>
 * If an adminKey is updated, the transaction must be signed by the pre-update adminKey and post-update adminKey.
 * <p>
 * If a new autoRenewAccount is specified (not just being removed), that account must also sign the transaction.
 */
public final class TopicUpdateTransaction extends Transaction<TopicUpdateTransaction> {
    private final ConsensusUpdateTopicTransactionBody.Builder builder;

    public TopicUpdateTransaction() {
        builder = ConsensusUpdateTopicTransactionBody.newBuilder();
    }

    TopicUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getConsensusUpdateTopic().toBuilder();
    }

    @Nullable
    public TopicId getTopicId() {
        return builder.hasTopicID() ? TopicId.fromProtobuf(builder.getTopicID()) : null;
    }

    /**
     * Set the topic ID to update.
     *
     * @param topicId The TopicId to be set
     * @return {@code this}
     */
    public TopicUpdateTransaction setTopicId(TopicId topicId) {
        requireNotFrozen();
        builder.setTopicID(topicId.toProtobuf());
        return this;
    }

    @Nullable
    public String getTopicMemo() {
        return builder.hasMemo() ? builder.getMemo().getValue() : null;
    }

    /**
     * Set a new memo for this topic.
     *
     * @param memo The memo to be set
     * @return {@code this}
     */
    public TopicUpdateTransaction setTopicMemo(String memo) {
        requireNotFrozen();
        builder.setMemo(StringValue.of(memo));
        return this;
    }

    /**
     * Clear the memo for this topic.
     *
     * @return {@code this}
     */
    public TopicUpdateTransaction clearTopicMemo() {
        requireNotFrozen();
        builder.setMemo(StringValue.of(""));
        return this;
    }

    @Nullable
    public Key getAdminKey() {
        return builder.hasAdminKey() ? Key.fromProtobuf(builder.getAdminKey()) : null;
    }

    /**
     * Set a new admin key for this topic.
     *
     * @param adminKey The Key to be set
     * @return {@code this}
     */
    public TopicUpdateTransaction setAdminKey(Key adminKey) {
        requireNotFrozen();
        builder.setAdminKey(adminKey.toKeyProtobuf());
        return this;
    }

    /**
     * Clear the admin key for this topic.
     *
     * @return {@code this}
     */
    public TopicUpdateTransaction clearAdminKey() {
        requireNotFrozen();

        builder.setAdminKey(com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setKeyList(KeyList.getDefaultInstance())
            .build());

        return this;
    }

    @Nullable
    public Key getSubmitKey() {
        return builder.hasSubmitKey() ? Key.fromProtobuf(builder.getSubmitKey()) : null;
    }

    /**
     * Set a new submit key for this topic.
     *
     * @param submitKey The Key to be set
     * @return {@code this}
     */
    public TopicUpdateTransaction setSubmitKey(Key submitKey) {
        requireNotFrozen();
        builder.setSubmitKey(submitKey.toKeyProtobuf());
        return this;
    }

    /**
     * Clear the submit key for this topic.
     *
     * @return {@code this}
     */
    public TopicUpdateTransaction clearSubmitKey() {
        requireNotFrozen();
        builder.setSubmitKey(com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setKeyList(KeyList.getDefaultInstance())
            .build());

        return this;
    }

    @Nullable
    public Duration getAutoRenewPeriod() {
        return builder.hasAutoRenewPeriod() ? DurationConverter.fromProtobuf(builder.getAutoRenewPeriod()) : null;
    }

    /**
     * Set a new auto renew period for this topic.
     *
     * @param autoRenewPeriod The Duration to be set for auto renewal
     * @return {@code this}
     */
    public TopicUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        requireNotFrozen();
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        return this;
    }

    @Nullable
    public AccountId getAutoRenewAccountId() {
        return builder.hasAutoRenewAccount() ? AccountId.fromProtobuf(builder.getAutoRenewAccount()) : null;
    }

    /**
     * Set a new auto renew account ID for this topic.
     *
     * @param autoRenewAccountId The AccountId to be set for auto renewal
     * @return {@code this}
     */
    public TopicUpdateTransaction setAutoRenewAccountId(AccountId autoRenewAccountId) {
        requireNotFrozen();
        builder.setAutoRenewAccount(autoRenewAccountId.toProtobuf());
        return this;
    }

    /**
     * Clear the auto renew account ID for this topic.
     *
     * @param autoRenewAccountId The AccountId to be cleared for auto renewal
     * @return {@code this}
     */
    public TopicUpdateTransaction clearAutoRenewAccountId(AccountId autoRenewAccountId) {
        requireNotFrozen();
        builder.setAutoRenewAccount(AccountID.getDefaultInstance());
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ConsensusServiceGrpc.getUpdateTopicMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusUpdateTopic(builder);
        return true;
    }
}
