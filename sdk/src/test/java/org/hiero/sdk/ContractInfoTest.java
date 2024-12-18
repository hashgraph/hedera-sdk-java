// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jsonSnapshot.SnapshotMatcher;
import java.time.Duration;
import java.time.Instant;
import org.bouncycastle.util.encoders.Hex;
import org.hiero.sdk.proto.ContractGetInfoResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ContractInfoTest {
    private final ContractGetInfoResponse.ContractInfo info = ContractGetInfoResponse.ContractInfo.newBuilder()
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
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void fromProtobuf() {
        SnapshotMatcher.expect(ContractInfo.fromProtobuf(info).toString()).toMatchSnapshot();
    }

    @Test
    void toProtobuf() {
        SnapshotMatcher.expect(ContractInfo.fromProtobuf(info).toProtobuf()).toMatchSnapshot();
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
