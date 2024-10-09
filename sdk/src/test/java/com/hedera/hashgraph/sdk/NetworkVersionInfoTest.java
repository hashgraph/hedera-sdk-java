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

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NetworkVersionInfoTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    NetworkVersionInfo spawnNetworkVerionInfoExample() {
        return new NetworkVersionInfo(new SemanticVersion(1, 2, 3), new SemanticVersion(4, 5, 6));
    }

    @Test
    void shouldSerialize() throws Exception {
        var originalNetworkVersionInfo = spawnNetworkVerionInfoExample();
        byte[] networkVersionInfoBytes = originalNetworkVersionInfo.toBytes();
        var copyNetworkVersionInfo = NetworkVersionInfo.fromBytes(networkVersionInfoBytes);
        assertThat(originalNetworkVersionInfo.toString().replaceAll("@[A-Za-z0-9]+", ""))
                .isEqualTo(copyNetworkVersionInfo.toString().replaceAll("@[A-Za-z0-9]+", ""));
        SnapshotMatcher.expect(originalNetworkVersionInfo.toString().replaceAll("@[A-Za-z0-9]+", ""))
                .toMatchSnapshot();
    }
}
