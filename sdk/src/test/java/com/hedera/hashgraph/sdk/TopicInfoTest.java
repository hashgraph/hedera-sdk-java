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

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ConsensusGetTopicInfoResponse;
import com.hedera.hashgraph.sdk.proto.ConsensusTopicInfo;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.bouncycastle.util.encoders.Hex;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;


public class TopicInfoTest {
    private static final PrivateKey privateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

    private static final byte[] hash = {2};

    private static final ConsensusGetTopicInfoResponse info = ConsensusGetTopicInfoResponse.newBuilder()
        .setTopicInfo(ConsensusTopicInfo.newBuilder()
            .setMemo("1")
            .setRunningHash(ByteString.copyFrom(hash))
            .setSequenceNumber(3)
            .setExpirationTime(InstantConverter.toProtobuf(Instant.ofEpochMilli(4)))
            .setAutoRenewPeriod(DurationConverter.toProtobuf(Duration.ofDays(5)))
            .setAdminKey(privateKey.getPublicKey().toProtobufKey())
            .setSubmitKey(privateKey.getPublicKey().toProtobufKey())
            .setAutoRenewAccount(new AccountId(4).toProtobuf())
            .setLedgerId(LedgerId.TESTNET.toByteString()))
        .build();


    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void fromProtobuf() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(TopicInfo.fromProtobuf(info).toString())
            .toMatchSnapshot();
    }

    @Test
    void toProtobuf() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(TopicInfo.fromProtobuf(info).toProtobuf().toString())
            .toMatchSnapshot();
    }

    @Test
    void fromBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(TopicInfo.fromBytes(info.toByteArray()).toString())
            .toMatchSnapshot();
    }

    @Test
    void toBytes() {
        SnapshotMatcher.expect(Hex.toHexString(TopicInfo.fromProtobuf(info).toBytes()))
            .toMatchSnapshot();
    }
}
