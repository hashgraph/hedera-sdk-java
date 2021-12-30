package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoGetInfoResponse;
import com.hedera.hashgraph.sdk.proto.KeyList;
import com.hedera.hashgraph.sdk.proto.LiveHash;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;


public class AccountInfoTest {
    private static final PrivateKey privateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");
    public static byte[] hash = {0, 1, 2};
    public static final LiveHash liveHash = LiveHash.newBuilder().setAccountId(new AccountId(10).toProtobuf())
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
    void fromProtobufWithOtherOptions() {
        SnapshotMatcher.expect(AccountInfo.fromProtobuf(info).toString())
            .toMatchSnapshot();
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
        SnapshotMatcher.expect(AccountInfo.fromProtobuf(info).toProtobuf())
            .toMatchSnapshot();
    }
}
