/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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
package com.hiero.sdk;

import com.hiero.sdk.proto.TokenType;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TokenTypeTest {

    private final TokenType tokenTypeFungible = TokenType.FUNGIBLE_COMMON;
    private final TokenType tokenTypeNonFungible = TokenType.NON_FUNGIBLE_UNIQUE;

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
                com.hiero.sdk.TokenType.valueOf(tokenTypeFungible).toString(),
                com.hiero.sdk.TokenType.valueOf(tokenTypeNonFungible).toString())
            .toMatchSnapshot();
    }

    @Test
    void toProtobuf() {
        SnapshotMatcher.expect(
                com.hiero.sdk.TokenType.valueOf(tokenTypeFungible).toProtobuf(),
                com.hiero.sdk.TokenType.valueOf(tokenTypeNonFungible).toProtobuf())
            .toMatchSnapshot();
    }
}
