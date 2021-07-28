package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.ConsensusUpdateTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.KeyList;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

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

    @Nullable
    TopicId topicId = null;
    @Nullable
    AccountId autoRenewAccountId = null;

    public TopicUpdateTransaction() {
        builder = ConsensusUpdateTopicTransactionBody.newBuilder();
    }

    TopicUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getConsensusUpdateTopic().toBuilder();

        if (builder.hasTopicID()) {
            topicId = TopicId.fromProtobuf(builder.getTopicID());
        }
    }

    TopicUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getConsensusUpdateTopic().toBuilder();

        if (builder.hasTopicID()) {
            topicId = TopicId.fromProtobuf(builder.getTopicID());
        }
    }

    @Nullable
    public TopicId getTopicId() {
        return topicId;
    }

    /**
     * Set the topic ID to update.
     *
     * @param topicId The TopicId to be set
     * @return {@code this}
     */
    public TopicUpdateTransaction setTopicId(TopicId topicId) {
        Objects.requireNonNull(topicId);
        requireNotFrozen();
        this.topicId = topicId;
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
        Objects.requireNonNull(memo);
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
        return builder.hasAdminKey() ? Key.fromProtobufKey(builder.getAdminKey()) : null;
    }

    /**
     * Set a new admin key for this topic.
     *
     * @param adminKey The Key to be set
     * @return {@code this}
     */
    public TopicUpdateTransaction setAdminKey(Key adminKey) {
        Objects.requireNonNull(adminKey);
        requireNotFrozen();
        builder.setAdminKey(adminKey.toProtobufKey());
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
        return builder.hasSubmitKey() ? Key.fromProtobufKey(builder.getSubmitKey()) : null;
    }

    /**
     * Set a new submit key for this topic.
     *
     * @param submitKey The Key to be set
     * @return {@code this}
     */
    public TopicUpdateTransaction setSubmitKey(Key submitKey) {
        Objects.requireNonNull(submitKey);
        requireNotFrozen();
        builder.setSubmitKey(submitKey.toProtobufKey());
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
        Objects.requireNonNull(autoRenewPeriod);
        requireNotFrozen();
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        return this;
    }

    @Nullable
    public AccountId getAutoRenewAccountId() {
        return autoRenewAccountId;
    }

    /**
     * Set a new auto renew account ID for this topic.
     *
     * @param autoRenewAccountId The AccountId to be set for auto renewal
     * @return {@code this}
     */
    public TopicUpdateTransaction setAutoRenewAccountId(AccountId autoRenewAccountId) {
        Objects.requireNonNull(autoRenewAccountId);
        requireNotFrozen();
        this.autoRenewAccountId = autoRenewAccountId;
        return this;
    }

    /**
     * @deprecated Use {@link #clearAutoRenewAccountId()}
     *
     * Clear the auto renew account ID for this topic.
     *
     * @param autoRenewAccountId The AccountId to be cleared for auto renewal
     * @return {@code this}
     */
    @SuppressWarnings("MissingSummary")
    @Deprecated
    public TopicUpdateTransaction clearAutoRenewAccountId(AccountId autoRenewAccountId) {
        return clearAutoRenewAccountId();
    }

    /**
     * Clear the auto renew account ID for this topic.
     *
     * @return {@code this}
     */
    public TopicUpdateTransaction clearAutoRenewAccountId() {
        requireNotFrozen();
        autoRenewAccountId = new AccountId(0);
        return this;
    }

    ConsensusUpdateTopicTransactionBody.Builder build() {
        if (topicId != null) {
            builder.setTopicID(topicId.toProtobuf());
        }

        if (autoRenewAccountId != null) {
            builder.setAutoRenewAccount(autoRenewAccountId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws InvalidChecksumException {
        if (topicId != null) {
            topicId.validateChecksum(client);
        }

        if ((autoRenewAccountId != null) &&
            ( ! autoRenewAccountId.equals(new AccountId(0)))
        ) {
            autoRenewAccountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ConsensusServiceGrpc.getUpdateTopicMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusUpdateTopic(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setConsensusUpdateTopic(build());
    }
}
