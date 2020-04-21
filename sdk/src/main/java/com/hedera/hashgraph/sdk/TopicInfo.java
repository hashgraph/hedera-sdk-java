package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ConsensusGetTopicInfoResponse;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;

public final class TopicInfo {
    /**
     * The ID of the topic for which information is requested.
     */
    public final TopicId topicId;

    /**
     * Short publicly visible memo about the topic. No guarantee of uniqueness.
     */
    public final String topicMemo;

    /**
     * SHA-384 running hash of (previousRunningHash, topicId, consensusTimestamp, sequenceNumber, message).
     */
    public final ByteString runningHash;

    /**
     * Sequence number (starting at 1 for the first submitMessage) of messages on the topic.
     */
    public final long sequenceNumber;

    /**
     * Effective consensus timestamp at (and after) which submitMessage calls will no longer succeed on the topic.
     */
    public final Instant expirationTime;

    /**
     * Access control for update/delete of the topic. Null if there is no key.
     */
    @Nullable
    public final Key adminKey;

    /**
     * Access control for ConsensusService.submitMessage. Null if there is no key.
     */
    @Nullable
    public final Key submitKey;

    public final Duration autoRenewPeriod;

    @Nullable
    public final AccountId autoRenewAccountId;

    private TopicInfo(
        TopicId topicId,
        String topicMemo,
        ByteString runningHash,
        long sequenceNumber,
        Instant expirationTime,
        @Nullable Key adminKey,
        @Nullable Key submitKey,
        Duration autoRenewPeriod,
        @Nullable AccountId autoRenewAccountId
    ) {
        this.topicId = topicId;
        this.topicMemo = topicMemo;
        this.runningHash = runningHash;
        this.sequenceNumber = sequenceNumber;
        this.expirationTime = expirationTime;
        this.adminKey = adminKey;
        this.submitKey = submitKey;
        this.autoRenewPeriod = autoRenewPeriod;
        this.autoRenewAccountId = autoRenewAccountId;
    }

    static TopicInfo fromProtobuf(ConsensusGetTopicInfoResponse topicInfoResponse) {
        var topicInfo = topicInfoResponse.getTopicInfo();

        var adminKey = topicInfo.hasAdminKey()
            ? Key.fromProtobuf(topicInfo.getAdminKey())
            : null;

        var submitKey = topicInfo.hasSubmitKey()
            ? Key.fromProtobuf(topicInfo.getSubmitKey())
            : null;

        var autoRenewAccountId = topicInfo.hasAutoRenewAccount()
            ? AccountId.fromProtobuf(topicInfo.getAutoRenewAccount())
            : null;

        return new TopicInfo(
            TopicId.fromProtobuf(topicInfoResponse.getTopicID()),
            topicInfo.getMemo(),
            topicInfo.getRunningHash(),
            topicInfo.getSequenceNumber(),
            InstantConverter.fromProtobuf(topicInfo.getExpirationTime()),
            adminKey,
            submitKey,
            DurationConverter.fromProtobuf(topicInfo.getAutoRenewPeriod()),
            autoRenewAccountId
        );
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("topicId", topicId)
            .add("topicMemo", topicMemo)
            .add("runningHash", runningHash)
            .add("sequenceNumber", sequenceNumber)
            .add("expirationTime", expirationTime)
            .add("adminKey", adminKey)
            .add("submitKey", submitKey)
            .add("autoRenewPeriod", autoRenewPeriod)
            .add("autoRenewAccountId", autoRenewAccountId)
            .toString();
    }
}
