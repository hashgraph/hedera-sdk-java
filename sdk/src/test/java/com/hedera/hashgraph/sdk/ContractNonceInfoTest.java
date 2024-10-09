/*
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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
 */

package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ContractNonceInfoTest {
    private final com.hedera.hashgraph.sdk.proto.ContractNonceInfo info =
            com.hedera.hashgraph.sdk.proto.ContractNonceInfo.newBuilder()
                    .setContractId(new ContractId(1).toProtobuf())
                    .setNonce(2)
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
        SnapshotMatcher.expect(ContractNonceInfo.fromProtobuf(info).toString()).toMatchSnapshot();
    }

    @Test
    void toProtobuf() {
        SnapshotMatcher.expect(ContractNonceInfo.fromProtobuf(info).toProtobuf())
                .toMatchSnapshot();
    }

    @Test
    void toBytes() {
        SnapshotMatcher.expect(
                        Hex.toHexString(ContractNonceInfo.fromProtobuf(info).toBytes()))
                .toMatchSnapshot();
    }

    @Test
    void fromBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(ContractNonceInfo.fromBytes(info.toByteArray()).toString())
                .toMatchSnapshot();
    }
}
