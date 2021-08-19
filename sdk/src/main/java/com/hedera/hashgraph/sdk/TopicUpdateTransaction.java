package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.ConsensusUpdateTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.time.Duration;

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
    @Nullable
    private TopicId topicId = null;
    @Nullable
    private AccountId autoRenewAccountId = null;
    @Nullable
    private String topicMemo = null;
    @Nullable
    private Key adminKey = null;
    @Nullable
    private Key submitKey = null;
    @Nullable
    private Duration autoRenewPeriod = null;

    public TopicUpdateTransaction() {
    }

    TopicUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TopicUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
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
        return topicMemo;
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
        topicMemo = memo;
        return this;
    }

    /**
     * Clear the memo for this topic.
     *
     * @return {@code this}
     */
    public TopicUpdateTransaction clearTopicMemo() {
        requireNotFrozen();
        topicMemo = "";
        return this;
    }

    @Nullable
    public Key getAdminKey() {
        return adminKey;
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
        this.adminKey = adminKey;
        return this;
    }

    /**
     * Clear the admin key for this topic.
     *
     * @return {@code this}
     */
    public TopicUpdateTransaction clearAdminKey() {
        requireNotFrozen();
        adminKey = new KeyList();
        return this;
    }

    @Nullable
    public Key getSubmitKey() {
        return submitKey;
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
        this.submitKey = submitKey;
        return this;
    }

    /**
     * Clear the submit key for this topic.
     *
     * @return {@code this}
     */
    public TopicUpdateTransaction clearSubmitKey() {
        requireNotFrozen();
        submitKey = new KeyList();
        return this;
    }

    @Nullable
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
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
        this.autoRenewPeriod = autoRenewPeriod;
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
     * @param autoRenewAccountId The AccountId to be cleared for auto renewal
     * @return {@code this}
     * @deprecated Use {@link #clearAutoRenewAccountId()}
     * <p>
     * Clear the auto renew account ID for this topic.
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

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getConsensusUpdateTopic();
        if (body.hasTopicID()) {
            topicId = TopicId.fromProtobuf(body.getTopicID());
        }
        if (body.hasAdminKey()) {
            adminKey = Key.fromProtobufKey(body.getAdminKey());
        }
        if (body.hasSubmitKey()) {
            submitKey = Key.fromProtobufKey(body.getSubmitKey());
        }
        if (body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }
        if (body.hasAutoRenewAccount()) {
            autoRenewAccountId = AccountId.fromProtobuf(body.getAutoRenewAccount());
        }
        if (body.hasMemo()) {
            topicMemo = body.getMemo().getValue();
        }
    }

    ConsensusUpdateTopicTransactionBody.Builder build() {
        var builder = ConsensusUpdateTopicTransactionBody.newBuilder();
        if (topicId != null) {
            builder.setTopicID(topicId.toProtobuf());
        }
        if (autoRenewAccountId != null) {
            builder.setAutoRenewAccount(autoRenewAccountId.toProtobuf());
        }
        if (adminKey != null) {
            builder.setAdminKey(adminKey.toProtobufKey());
        }
        if (submitKey != null) {
            builder.setSubmitKey(submitKey.toProtobufKey());
        }
        if (autoRenewPeriod != null) {
            builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        }
        if (topicMemo != null) {
            builder.setMemo(StringValue.of(topicMemo));
        }
        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (topicId != null) {
            topicId.validateChecksum(client);
        }

        if ((autoRenewAccountId != null) &&
            (!autoRenewAccountId.equals(new AccountId(0)))
        ) {
            autoRenewAccountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ConsensusServiceGrpc.getUpdateTopicMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusUpdateTopic(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setConsensusUpdateTopic(build());
    }
}
