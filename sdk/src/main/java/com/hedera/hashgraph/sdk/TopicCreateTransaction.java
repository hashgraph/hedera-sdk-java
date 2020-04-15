package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ConsensusCreateTopicTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import org.threeten.bp.Duration;

public final class TopicCreateTransaction extends TransactionBuilder<TopicCreateTransaction> {
    private final ConsensusCreateTopicTransactionBody.Builder builder;

    public TopicCreateTransaction() {
        builder = ConsensusCreateTopicTransactionBody.newBuilder();

        setAutoRenewPeriod(DEFAULT_AUTO_RENEW_PERIOD);
    }

    /**
     * Set a short publicly visible memo on the new topic.
     *
     * @return {@code this}.
     */
    public TopicCreateTransaction setTopicMemo(String memo) {
        builder.setMemo(memo);
        return this;
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
     * @return {@code this}.
     */
    public TopicCreateTransaction setAdminKey(Key adminKey) {
        builder.setAdminKey(adminKey.toKeyProtobuf());
        return this;
    }

    /**
     * Set the submit key for the new topic.
     * <p>
     * Access control for submitMessage.
     * If unspecified, no access control is performed on ConsensusService.submitMessage (all submissions are allowed).
     *
     * @return {@code this}.
     */
    public TopicCreateTransaction setSubmitKey(Key submitKey) {
        builder.setSubmitKey(submitKey.toKeyProtobuf());
        return this;
    }

    /**
     * Set the auto renew period for the new topic.
     * <p>
     * The initial lifetime of the topic and the amount of time to attempt to extend the topic's lifetime by
     * automatically at the topic's expirationTime, if the autoRenewAccount is configured (once autoRenew functionality
     * is supported by HAPI).
     *
     * @return {@code this}.
     */
    public TopicCreateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        return this;
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
     * @return {@code this}.
     */
    public TopicCreateTransaction setAutoRenewAccountId(AccountId autoRenewAccountId) {
        builder.setAutoRenewAccount(autoRenewAccountId.toProtobuf());
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setConsensusCreateTopic(builder);
    }
}
