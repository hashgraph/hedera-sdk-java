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

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ConsensusGetTopicInfoResponse;
import com.hedera.hashgraph.sdk.proto.ConsensusTopicInfo;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;

/**
 * Current state of a topic.
 */
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

    /**
     * The ledger ID the response was returned from; please see <a href="https://github.com/hashgraph/hedera-improvement-proposal/blob/master/HIP/hip-198.md">HIP-198</a> for the network-specific IDs.
     */
    public final LedgerId ledgerId;

    private TopicInfo(
        TopicId topicId,
        String topicMemo,
        ByteString runningHash,
        long sequenceNumber,
        Instant expirationTime,
        @Nullable Key adminKey,
        @Nullable Key submitKey,
        Duration autoRenewPeriod,
        @Nullable AccountId autoRenewAccountId,
        LedgerId ledgerId
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
        this.ledgerId = ledgerId;
    }

    static TopicInfo fromProtobuf(ConsensusGetTopicInfoResponse topicInfoResponse) {
        var topicInfo = topicInfoResponse.getTopicInfo();

        var adminKey = topicInfo.hasAdminKey()
            ? Key.fromProtobufKey(topicInfo.getAdminKey())
            : null;

        var submitKey = topicInfo.hasSubmitKey()
            ? Key.fromProtobufKey(topicInfo.getSubmitKey())
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
            autoRenewAccountId,
            LedgerId.fromByteString(topicInfo.getLedgerId())
        );
    }

    public static TopicInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(ConsensusGetTopicInfoResponse.parseFrom(bytes).toBuilder().build());
    }

    ConsensusGetTopicInfoResponse toProtobuf() {
        var topicInfoResponseBuilder = ConsensusGetTopicInfoResponse.newBuilder()
            .setTopicID(topicId.toProtobuf());

        var topicInfoBuilder = ConsensusTopicInfo.newBuilder()
            .setMemo(topicMemo)
            .setRunningHash(runningHash)
            .setSequenceNumber(sequenceNumber)
            .setExpirationTime(InstantConverter.toProtobuf(expirationTime))
            .setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod))
            .setLedgerId(ledgerId.toByteString());

        if (adminKey != null) {
            topicInfoBuilder.setAdminKey(adminKey.toProtobufKey());
        }

        if (submitKey != null) {
            topicInfoBuilder.setSubmitKey(submitKey.toProtobufKey());
        }

        if (autoRenewAccountId != null) {
            topicInfoBuilder.setAutoRenewAccount(autoRenewAccountId.toProtobuf());
        }

        return topicInfoResponseBuilder.setTopicInfo(topicInfoBuilder).build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("topicId", topicId)
            .add("topicMemo", topicMemo)
            .add("runningHash", runningHash.toByteArray())
            .add("sequenceNumber", sequenceNumber)
            .add("expirationTime", expirationTime)
            .add("adminKey", adminKey)
            .add("submitKey", submitKey)
            .add("autoRenewPeriod", autoRenewPeriod)
            .add("autoRenewAccountId", autoRenewAccountId)
            .add("ledgerId", ledgerId)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
