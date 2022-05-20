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

import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NftIdTest {

    static Client mainnetClient;
    static Client testnetClient;
    static Client previewnetClient;

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
        mainnetClient = Client.forMainnet();
        testnetClient = Client.forTestnet();
        previewnetClient = Client.forPreviewnet();
    }

    @AfterClass
    public static void afterAll() throws TimeoutException {
        mainnetClient.close();
        testnetClient.close();
        previewnetClient.close();
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void fromString() {
        SnapshotMatcher.expect(NftId.fromString("0.0.5005@1234").toString()).toMatchSnapshot();
    }

    @Test
    void fromString2() {
        SnapshotMatcher.expect(NftId.fromString("0.0.5005/1234").toString()).toMatchSnapshot();
    }

    @Test
    void toFromString() {
        var id1 = NftId.fromString("0.0.5005@1234");
        var id2 = NftId.fromString(id1.toString());
        assertEquals(id1.toString(), id2.toString());
    }

    @Test
    void fromStringWithChecksumOnMainnet() {
        SnapshotMatcher.expect(NftId.fromString("0.0.123-vfmkw/7584").toStringWithChecksum(mainnetClient)).toMatchSnapshot();
    }

    @Test
    void fromStringWithChecksumOnTestnet() {
        SnapshotMatcher.expect(NftId.fromString("0.0.123-esxsf@584903").toStringWithChecksum(testnetClient)).toMatchSnapshot();
    }

    @Test
    void fromStringWithChecksumOnPreviewnet() {
        SnapshotMatcher.expect(NftId.fromString("0.0.123-ogizo/487302").toStringWithChecksum(previewnetClient)).toMatchSnapshot();
    }

    @Test
    void toBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(Hex.toHexString(new TokenId(5005).nft(4920).toBytes())).toMatchSnapshot();
    }

    @Test
    void fromBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(NftId.fromBytes(new TokenId(5005).nft(574489).toBytes()).toString()).toMatchSnapshot();
    }
}
