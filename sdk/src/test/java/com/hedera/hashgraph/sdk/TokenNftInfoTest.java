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
import javax.annotation.Nullable;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TokenNftInfoTest {
    static final Instant creationTime = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    private static TokenNftInfo spawnTokenNftInfoExample(@Nullable AccountId spenderAccountId) {
        return new TokenNftInfo(
                TokenId.fromString("1.2.3").nft(4),
                AccountId.fromString("5.6.7"),
                creationTime,
                Hex.decode("deadbeef"),
                LedgerId.MAINNET,
                spenderAccountId);
    }

    @Test
    void shouldSerialize() throws Exception {
        var originalTokenInfo = spawnTokenNftInfoExample(AccountId.fromString("8.9.10"));
        byte[] tokenInfoBytes = originalTokenInfo.toBytes();
        var copyTokenInfo = TokenNftInfo.fromBytes(tokenInfoBytes);
        assertThat(copyTokenInfo.toString()).isEqualTo(originalTokenInfo.toString());
        SnapshotMatcher.expect(originalTokenInfo.toString()).toMatchSnapshot();
    }

    @Test
    void shouldSerializeNullSpender() throws Exception {
        var originalTokenInfo = spawnTokenNftInfoExample(null);
        byte[] tokenInfoBytes = originalTokenInfo.toBytes();
        var copyTokenInfo = TokenNftInfo.fromBytes(tokenInfoBytes);
        assertThat(copyTokenInfo.toString()).isEqualTo(originalTokenInfo.toString());
        SnapshotMatcher.expect(originalTokenInfo.toString()).toMatchSnapshot();
    }
}
