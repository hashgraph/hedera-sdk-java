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
import java.time.Instant;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StakingInfoTest {
    final Instant validStart = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    StakingInfo spawnStakingInfoAccountExample() {
        return new StakingInfo(true, validStart, Hbar.from(5), Hbar.from(10), AccountId.fromString("1.2.3"), null);
    }

    StakingInfo spawnStakingInfoNodeExample() {
        return new StakingInfo(true, validStart, Hbar.from(5), Hbar.from(10), null, 3L);
    }

    @Test
    void shouldSerializeAccount() throws Exception {
        var originalStakingInfo = spawnStakingInfoAccountExample();
        byte[] stakingInfoBytes = originalStakingInfo.toBytes();
        var copyStakingInfo = StakingInfo.fromBytes(stakingInfoBytes);
        assertThat(copyStakingInfo.toString().replaceAll("@[A-Za-z0-9]+", ""))
                .isEqualTo(originalStakingInfo.toString().replaceAll("@[A-Za-z0-9]+", ""));
        SnapshotMatcher.expect(originalStakingInfo.toString().replaceAll("@[A-Za-z0-9]+", ""))
                .toMatchSnapshot();
    }

    @Test
    void shouldSerializeNode() throws Exception {
        var originalStakingInfo = spawnStakingInfoNodeExample();
        byte[] stakingInfoBytes = originalStakingInfo.toBytes();
        var copyStakingInfo = StakingInfo.fromBytes(stakingInfoBytes);
        assertThat(copyStakingInfo.toString().replaceAll("@[A-Za-z0-9]+", ""))
                .isEqualTo(originalStakingInfo.toString().replaceAll("@[A-Za-z0-9]+", ""));
        SnapshotMatcher.expect(originalStakingInfo.toString().replaceAll("@[A-Za-z0-9]+", ""))
                .toMatchSnapshot();
    }
}
