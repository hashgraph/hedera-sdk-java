// SPDX-License-Identifier: Apache-2.0
package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ConsensusCreateTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Create a topic to be used for consensus.
 * <p>
 * If an autoRenewAccount is specified, that account must also sign this transaction.
 * <p>
 * If an adminKey is specified, the adminKey must sign the transaction.
 * <p>
 * On success, the resulting TransactionReceipt contains the newly created TopicId.
 *
 * See <a href="https://docs.hedera.com/guides/docs/hedera-api/consensus-service/consensus-service">Hedera Documentation</a>
 * See <a href="https://docs.hedera.com/guides/docs/hedera-api/consensus-service/consensuscreatetopic">Hedera Documentation</a>
 */
public final class TopicCreateTransaction extends Transaction<TopicCreateTransaction> {
    /**
     * Optional account to be used at the topic's expirationTime to extend
     * the life of the topic (once autoRenew functionality is supported by
     * HAPI).
     *
     * The topic lifetime will be extended up to a maximum of the
     * autoRenewPeriod or however long the topic can be extended using all
     * funds on the account (whichever is the smaller duration/amount and
     * if any extension is possible with the account's funds).
     * If specified, there must be an adminKey and the autoRenewAccount
     * must sign this transaction.
     */
    @Nullable
    private AccountId autoRenewAccountId = null;
    /**
     * The initial lifetime of the topic and the amount of time to attempt
     * to extend the topic's lifetime by,automatically at the topic's
     * expirationTime if the autoRenewAccount is configured (once autoRenew
     * functionality is supported by HAPI). Limited to MIN_AUTORENEW_PERIOD
     * and MAX_AUTORENEW_PERIOD value by server-side configuration.
     * Required.
     */
    @Nullable
    private Duration autoRenewPeriod = null;
    /**
     * Short publicly visible memo about the topic.
     *
     * No guarantee of uniqueness.
     *
     * 100 bytes max.
     */
    private String topicMemo = "";
    /**
     * Access control for updateTopic/deleteTopic. Anyone can increase the
     * topic's expirationTime via ConsensusService.updateTopic(), regardless
     * of the adminKey. If no adminKey is specified, updateTopic may only be
     * used to extend the topic's expirationTime, and deleteTopic is disallowed.
     */
    @Nullable
    private Key adminKey = null;
    /**
     * Access control for submitMessage. If unspecified, no access control
     * is performed on ConsensusService.submitMessage (all submissions are
     * allowed).
     */
    @Nullable
    private Key submitKey = null;

    private Key feeScheduleKey = null;

    private List<Key> feeExemptKeys = new ArrayList<>();

    private List<CustomFixedFee> customFees = new ArrayList<>();
    /**
     * Constructor.
     */
    public TopicCreateTransaction() {
        setAutoRenewPeriod(DEFAULT_AUTO_RENEW_PERIOD);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TopicCreateTransaction(
            LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TopicCreateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the topic memo.
     *
     * @return                          the topic memo
     */
    public String getTopicMemo() {
        return topicMemo;
    }

    /**
     * Set a short publicly visible memo on the new topic.
     *
     * @param memo The memo to be set
     * @return {@code this}
     */
    public TopicCreateTransaction setTopicMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        topicMemo = memo;
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
     * Set the admin key for the new topic.
     * <p>
     * Access control for updateTopic/deleteTopic.
     * <p>
     * Anyone can increase the topic's expirationTime regardless of the adminKey.
     * If no adminKey is specified, updateTopic may only be used to extend the topic's expirationTime, and deleteTopic
     * is disallowed.
     *
     * @param adminKey The Key to be set
     * @return {@code this}
     */
    public TopicCreateTransaction setAdminKey(Key adminKey) {
        Objects.requireNonNull(adminKey);
        requireNotFrozen();
        this.adminKey = adminKey;
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
     * Set the submit key for the new topic.
     * <p>
     * Access control for submitMessage.
     * If unspecified, no access control is performed on ConsensusService.submitMessage (all submissions are allowed).
     *
     * @param submitKey The Key to be set
     * @return {@code this}
     */
    public TopicCreateTransaction setSubmitKey(Key submitKey) {
        Objects.requireNonNull(submitKey);
        requireNotFrozen();
        this.submitKey = submitKey;
        return this;
    }

    /**
     * Extract the auto renew period.
     *
     * @return                          the auto renew period
     */
    @Nullable
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Set the auto renew period for the new topic.
     * <p>
     * The initial lifetime of the topic and the amount of time to attempt to extend the topic's lifetime by
     * automatically at the topic's expirationTime, if the autoRenewAccount is configured (once autoRenew functionality
     * is supported by HAPI).
     *
     * @param autoRenewPeriod The Duration to be set for auto renewal
     * @return {@code this}
     */
    public TopicCreateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
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
     * Set the ID of the auto renew account for the new topic.
     * <p>
     * Optional account to be used at the topic's expirationTime to extend the life of the topic (once autoRenew
     * functionality is supported by HAPI).
     * <p>
     * The topic lifetime will be extended up to a maximum of the autoRenewPeriod or however long the topic
     * can be extended using all funds on the account (whichever is the smaller duration/amount and if any extension
     * is possible with the account's funds).
     * <p>
     * If specified, there must be an adminKey and the autoRenewAccount must sign this transaction.
     *
     * @param autoRenewAccountId The AccountId to be set for auto renewal
     * @return {@code this}
     */
    public TopicCreateTransaction setAutoRenewAccountId(AccountId autoRenewAccountId) {
        Objects.requireNonNull(autoRenewAccountId);
        requireNotFrozen();
        this.autoRenewAccountId = autoRenewAccountId;
        return this;
    }

    /**
     * Returns the key which allows updates to the new topic’s fees.
     * @return the feeScheduleKey
     */
    public Key getFeeScheduleKey() {
        return feeScheduleKey;
    }

    /**
     * Sets the key which allows updates to the new topic’s fees.
     * @param feeScheduleKey the feeScheduleKey to be set
     * @return {@code this}
     */
    public TopicCreateTransaction setFeeScheduleKey(Key feeScheduleKey) {
        Objects.requireNonNull(feeScheduleKey);
        requireNotFrozen();
        this.feeScheduleKey = feeScheduleKey;
        return this;
    }

    /**
     * Returns the keys that will be exempt from paying fees.
     * @return the feeExemptKeys
     */
    public List<Key> getFeeExemptKeys() {
        return feeExemptKeys;
    }

    /**
     * Sets the keys that will be exempt from paying fees.
     * @param feeExemptKeys the keys to be set
     * @return {@code this}
     */
    public TopicCreateTransaction setFeeExemptKeys(List<Key> feeExemptKeys) {
        Objects.requireNonNull(feeExemptKeys);
        requireNotFrozen();
        this.feeExemptKeys = feeExemptKeys;
        return this;
    }

    /**
     * Clears all keys that will be exempt from paying fees.
     * @return {@code this}
     */
    public TopicCreateTransaction clearFeeExemptKeys() {
        requireNotFrozen();
        this.feeExemptKeys.clear();
        return this;
    }

    /**
     * Adds a key that will be exempt from paying fees.
     * @param feeExemptKey feeExemptKey
     * @return {@code this}
     */
    public TopicCreateTransaction addFeeExemptKey(Key feeExemptKey) {
        Objects.requireNonNull(feeExemptKey);
        requireNotFrozen();
        if (feeExemptKeys != null) {
            feeExemptKeys.add(feeExemptKey);
        }
        return this;
    }

    /**
     * Returns the fixed fees to assess when a message is submitted to the new topic.
     * @return the List<CustomFixedFee>
     */
    public List<CustomFixedFee> getCustomFees() {
        return customFees;
    }

    /**
     * Sets the fixed fees to assess when a message is submitted to the new topic.
     *
     * @param  customFees List of CustomFixedFee
     * @return {@code this}
     */
    public TopicCreateTransaction setCustomFees(List<CustomFixedFee> customFees) {
        Objects.requireNonNull(customFees);
        requireNotFrozen();
        this.customFees = customFees;
        return this;
    }

    /**
     * Clears fixed fees.
     *
     * @return {@code this}
     */
    public TopicCreateTransaction clearCustomFees() {
        requireNotFrozen();
        this.customFees = new ArrayList<>();
        return this;
    }

    /**
     * Adds fixed fee to assess when a message is submitted to the new topic.
     *
     * @param  customFixedFee CustomFixedFee
     * @return {@code this}
     */
    public TopicCreateTransaction addCustomFee(CustomFixedFee customFixedFee) {
        Objects.requireNonNull(customFees);
        customFees.add(customFixedFee);
        requireNotFrozen();
        return this;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getConsensusCreateTopic();
        if (body.hasAutoRenewAccount()) {
            autoRenewAccountId = AccountId.fromProtobuf(body.getAutoRenewAccount());
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
        if (body.hasFeeScheduleKey()) {
            feeScheduleKey = Key.fromProtobufKey(body.getFeeScheduleKey());
        }
        if (body.getFeeExemptKeyListList() != null) {
            feeExemptKeys = body.getFeeExemptKeyListList().stream()
                    .map(Key::fromProtobufKey)
                    .collect(Collectors.toList());
        }
        if (body.getCustomFeesList() != null) {
            customFees = body.getCustomFeesList().stream()
                    .map(x -> CustomFixedFee.fromProtobuf(x.getFixedFee()))
                    .collect(Collectors.toList());
        }
        topicMemo = body.getMemo();
    }

    /**
     * Build the transaction body.
     *
     * @return {@link
     *         com.hedera.hashgraph.sdk.proto.ConsensusCreateTopicTransactionBody}
     */
    ConsensusCreateTopicTransactionBody.Builder build() {
        var builder = ConsensusCreateTopicTransactionBody.newBuilder();
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
        if (feeScheduleKey != null) {
            builder.setFeeScheduleKey(feeScheduleKey.toProtobufKey());
        }
        if (feeExemptKeys != null) {
            for (var feeExemptKey : feeExemptKeys) {
                builder.addFeeExemptKeyList(feeExemptKey.toProtobufKey());
            }
        }
        if (customFees != null) {
            for (CustomFixedFee customFee : customFees) {
                builder.addCustomFees(customFee.toTopicFeeProtobuf());
            }
        }

        builder.setMemo(topicMemo);

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (autoRenewAccountId != null) {
            autoRenewAccountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ConsensusServiceGrpc.getCreateTopicMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusCreateTopic(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setConsensusCreateTopic(build());
    }
}
