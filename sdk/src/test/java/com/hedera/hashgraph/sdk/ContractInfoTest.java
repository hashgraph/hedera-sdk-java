package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractGetInfoResponse;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.bouncycastle.util.encoders.Hex;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

public class ContractInfoTest {
    private final ContractGetInfoResponse.ContractInfo info =
        ContractGetInfoResponse.ContractInfo.newBuilder()
            .setContractID(new ContractId(1).toProtobuf())
            .setAccountID(new AccountId(2).toProtobuf())
            .setContractAccountID("3")
            .setExpirationTime(InstantConverter.toProtobuf(Instant.ofEpochMilli(4)))
            .setAutoRenewPeriod(DurationConverter.toProtobuf(Duration.ofDays(5)))
            .setStorage(6)
            .setMemo("7")
            .setBalance(8)
            .setLedgerId(LedgerId.TESTNET.toByteString())
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
    void fromProtobuf() {
        SnapshotMatcher.expect(ContractInfo.fromProtobuf(info).toString())
            .toMatchSnapshot();
    }

    @Test
    void toProtobuf() {
        SnapshotMatcher.expect(ContractInfo.fromProtobuf(info).toProtobuf())
            .toMatchSnapshot();
    }

    @Test
    void toBytes() {
        SnapshotMatcher.expect(Hex.toHexString(ContractInfo.fromProtobuf(info).toBytes()))
            .toMatchSnapshot();
    }

    @Test
    void fromBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(ContractInfo.fromBytes(info.toByteArray()).toString())
            .toMatchSnapshot();
    }
}
