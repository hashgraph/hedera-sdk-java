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
