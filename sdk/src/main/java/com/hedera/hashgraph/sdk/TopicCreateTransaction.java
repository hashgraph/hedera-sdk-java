package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ConsensusCreateTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Create a topic to be used for consensus.
 * <p>
 * If an autoRenewAccount is specified, that account must also sign this transaction.
 * <p>
 * If an adminKey is specified, the adminKey must sign the transaction.
 * <p>
 * On success, the resulting TransactionReceipt contains the newly created TopicId.
 */
public final class TopicCreateTransaction extends Transaction<TopicCreateTransaction> {
    @Nullable
    private AccountId autoRenewAccountId = null;
    @Nullable
    private Duration autoRenewPeriod = null;
    private String topicMemo = "";
    @Nullable
    private Key adminKey = null;
    @Nullable
    private Key submitKey = null;

    public TopicCreateTransaction() {
        setAutoRenewPeriod(DEFAULT_AUTO_RENEW_PERIOD);
    }

    TopicCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TopicCreateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

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

    void initFromTransactionBody() {
        var body = txBody.getConsensusCreateTopic();
        if (body.hasAutoRenewAccount()) {
            autoRenewAccountId = AccountId.fromProtobuf(body.getAutoRenewAccount());
        }
        if(body.hasAdminKey()) {
            adminKey = Key.fromProtobufKey(body.getAdminKey());
        }
        if(body.hasSubmitKey()) {
            submitKey = Key.fromProtobufKey(body.getSubmitKey());
        }
        if(body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }
        topicMemo = body.getMemo();
    }

    ConsensusCreateTopicTransactionBody.Builder build() {
        var builder = ConsensusCreateTopicTransactionBody.newBuilder();
        if (autoRenewAccountId != null) {
            builder.setAutoRenewAccount(autoRenewAccountId.toProtobuf());
        }
        if(adminKey != null) {
            builder.setAdminKey(adminKey.toProtobufKey());
        }
        if(submitKey != null) {
            builder.setSubmitKey(submitKey.toProtobufKey());
        }
        if(autoRenewPeriod != null) {
            builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
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
