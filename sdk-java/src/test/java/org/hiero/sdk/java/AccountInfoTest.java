// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jsonSnapshot.SnapshotMatcher;
import java.time.Duration;
import java.time.Instant;
import org.hiero.sdk.java.proto.CryptoGetInfoResponse;
import org.hiero.sdk.java.proto.KeyList;
import org.hiero.sdk.java.proto.LiveHash;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AccountInfoTest {
    private static final PrivateKey privateKey = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");
    private static final byte[] hash = {0, 1, 2};
    private static final LiveHash liveHash = LiveHash.newBuilder()
            .setAccountId(new AccountId(10).toProtobuf())
            .setDuration(DurationConverter.toProtobuf(Duration.ofDays(11)))
            .setHash(ByteString.copyFrom(hash))
            .setKeys(KeyList.newBuilder().addKeys(privateKey.getPublicKey().toProtobufKey()))
            .build();
    private static final CryptoGetInfoResponse.AccountInfo info = CryptoGetInfoResponse.AccountInfo.newBuilder()
            .setAccountID(new AccountId(1).toProtobuf())
            .setDeleted(true)
            .setProxyReceived(2)
            .setKey(privateKey.getPublicKey().toProtobufKey())
            .setBalance(3)
            .setGenerateSendRecordThreshold(4)
            .setGenerateReceiveRecordThreshold(5)
            .setReceiverSigRequired(true)
            .setExpirationTime(InstantConverter.toProtobuf(Instant.ofEpochMilli(6)))
            .setAutoRenewPeriod(DurationConverter.toProtobuf(Duration.ofDays(7)))
            .setProxyAccountID(new AccountId(8).toProtobuf())
            .addLiveHashes(liveHash)
            .setLedgerId(LedgerId.PREVIEWNET.toByteString())
            .setEthereumNonce(1001)
            .build();

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void fromProtobufWithOtherOptions() {
        SnapshotMatcher.expect(AccountInfo.fromProtobuf(info).toString()).toMatchSnapshot();
    }

    @Test
    void fromBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(AccountInfo.fromBytes(info.toByteArray()).toString())
                .toMatchSnapshot();
    }

    @Test
    void toBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(AccountInfo.fromBytes(info.toByteArray()).toBytes())
                .toMatchSnapshot();
    }

    @Test
    void toProtobuf() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(AccountInfo.fromProtobuf(info).toProtobuf().toString())
                .toMatchSnapshot();
    }
}
