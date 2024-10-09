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

import com.hedera.hashgraph.sdk.proto.TokenSupplyType;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TokenSupplyTypeTest {
    private final TokenSupplyType tokenSupplyTypeInfinite = TokenSupplyType.INFINITE;
    private final TokenSupplyType tokenSupplyTypeFinite = TokenSupplyType.FINITE;

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
        SnapshotMatcher.expect(
                        com.hedera.hashgraph.sdk.TokenSupplyType.valueOf(tokenSupplyTypeInfinite)
                                .toString(),
                        com.hedera.hashgraph.sdk.TokenSupplyType.valueOf(tokenSupplyTypeFinite)
                                .toString())
                .toMatchSnapshot();
    }

    @Test
    void toProtobuf() {
        SnapshotMatcher.expect(
                        com.hedera.hashgraph.sdk.TokenSupplyType.valueOf(tokenSupplyTypeInfinite)
                                .toProtobuf(),
                        com.hedera.hashgraph.sdk.TokenSupplyType.valueOf(tokenSupplyTypeFinite)
                                .toProtobuf())
                .toMatchSnapshot();
    }

    @Test
    void tokenSupplyTestToString() {
        assertThat(com.hedera.hashgraph.sdk.TokenSupplyType.INFINITE).hasToString("INFINITE");
        assertThat(com.hedera.hashgraph.sdk.TokenSupplyType.FINITE).hasToString("FINITE");
    }
}
