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
import io.github.jsonSnapshot.SnapshotMatcher;
import org.bouncycastle.util.encoders.Hex;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ContractIdTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void fromString() {
        SnapshotMatcher.expect(ContractId.fromString("0.0.5005").toString()).toMatchSnapshot();
    }

    @Test
    void fromSolidityAddress() {
        SnapshotMatcher.expect(ContractId.fromSolidityAddress("000000000000000000000000000000000000138D").toString()).toMatchSnapshot();
    }

    @Test
    void fromSolidityAddressWith0x() {
        SnapshotMatcher.expect(ContractId.fromSolidityAddress("0x000000000000000000000000000000000000138D").toString()).toMatchSnapshot();
    }

    @Test
    void fromEvmAddress() {
        SnapshotMatcher.expect(ContractId.fromEvmAddress(1, 2, "98329e006610472e6B372C080833f6D79ED833cf").toString()).toMatchSnapshot();
    }

    @Test
    void fromEvmAddressWith0x() {
        SnapshotMatcher.expect(ContractId.fromEvmAddress(1, 2, "0x98329e006610472e6B372C080833f6D79ED833cf").toString()).toMatchSnapshot();
    }

    @Test
    void fromStringWithEvmAddress() {
        SnapshotMatcher.expect(ContractId.fromString("1.2.98329e006610472e6B372C080833f6D79ED833cf").toString()).toMatchSnapshot();
    }

    @Test
    void toFromBytes() throws InvalidProtocolBufferException {
        ContractId a = ContractId.fromString("1.2.3");
        assertThat(ContractId.fromBytes(a.toBytes())).isEqualTo(a);
        ContractId b = ContractId.fromEvmAddress(1, 2, "0x98329e006610472e6B372C080833f6D79ED833cf");
        assertThat(ContractId.fromBytes(b.toBytes())).isEqualTo(b);
    }

    @Test
    void toBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(Hex.toHexString(new ContractId(5005).toBytes())).toMatchSnapshot();
    }

    @Test
    void fromBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(ContractId.fromBytes(new ContractId(5005).toBytes()).toString()).toMatchSnapshot();
    }

    @Test
    void toSolidityAddress() {
        SnapshotMatcher.expect(new ContractId(5005).toSolidityAddress()).toMatchSnapshot();
    }

    @Test
    void toSolidityAddress2() {
        SnapshotMatcher.expect(ContractId.fromEvmAddress(1, 2, "0x98329e006610472e6B372C080833f6D79ED833cf").toSolidityAddress()).toMatchSnapshot();
    }
}
