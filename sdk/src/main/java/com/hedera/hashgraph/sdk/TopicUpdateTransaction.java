/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.ConsensusUpdateTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

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
    /**
     * Update the topic ID
     */
    @Nullable
    private TopicId topicId;
    /**
     * Set a new auto-renew account ID for this topic (once autoRenew
     * functionality is supported by HAPI).
     */
    @Nullable
    private AccountId autoRenewAccountId;
    /**
     * Set a new short publicly visible memo on the new topic and is stored
     * with the topic. (100 bytes)
     */
    @Nullable
    private String topicMemo;
    /**
     * Set a new admin key that authorizes update topic and delete topic
     * transactions.
     */
    @Nullable
    private Key adminKey;
    /**
     * Set a new submit key for a topic that authorizes sending messages
     * to this topic.
     */
    @Nullable
    private Key submitKey;
    /**
     * Set a new auto -enew period for this topic (once autoRenew
     * functionality is supported by HAPI).
     */
    @Nullable
    private Duration autoRenewPeriod;

    @Nullable
    private Instant expirationTime;

    /**
     * Constructor.
     */
    public TopicUpdateTransaction() {
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TopicUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TopicUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the topic id.
     *
     * @return                          the topic id
     */
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

    /**
     * Extract the topic memo.
     *
     * @return                          the topic memo
     */
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

    /**
     * Extract the admin key.
     *
     * @return                          the admin key
     */
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

    /**
     * Extract the submit key.
     *
     * @return                          the submit key
     */
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

    /**
     * Extract the auto renew period.
     *
     * @return                          the auto renew period
     */
    @Nullable
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "A Duration can't actually be mutated"
    )
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Set a new auto renew period for this topic.
     *
     * @param autoRenewPeriod The Duration to be set for auto renewal
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "A Duration can't actually be mutated"
    )
    public TopicUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        Objects.requireNonNull(autoRenewPeriod);
        requireNotFrozen();
        this.autoRenewPeriod = autoRenewPeriod;
        return this;
    }

    /**
     * Extract the auto renew account id.
     *
     * @return                          the auto renew account id
     */
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

    /**
     * @return Expiration time
     */
    @Nullable
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "An Instant can't actually be mutated"
    )
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * Sets the effective consensus timestamp at (and after) which all consensus transactions and queries will fail.
     * The expirationTime may be no longer than MAX_AUTORENEW_PERIOD (8000001 seconds) from the consensus timestamp of
     * this transaction.
     * On topics with no adminKey, extending the expirationTime is the only updateTopic option allowed on the topic.
     * @param expirationTime the new expiration time
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "An Instant can't actually be mutated"
    )
    public TopicUpdateTransaction setExpirationTime(Instant expirationTime) {
        requireNotFrozen();
        this.expirationTime = Objects.requireNonNull(expirationTime);
        return this;
    }

    /**
     * Initialize from the transaction body.
     */
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
        if (body.hasExpirationTime()) {
            expirationTime = InstantConverter.fromProtobuf(body.getExpirationTime());
        }
    }

    /**
     * Build the transaction body.
     *
     * @return {@link
     *         com.hedera.hashgraph.sdk.proto.ConsensusUpdateTopicTransactionBody}
     */
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
        if (expirationTime != null) {
            builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        }
        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (topicId != null) {
            topicId.validateChecksum(client);
        }

        if ((autoRenewAccountId != null) &&
            !autoRenewAccountId.equals(new AccountId(0))
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
