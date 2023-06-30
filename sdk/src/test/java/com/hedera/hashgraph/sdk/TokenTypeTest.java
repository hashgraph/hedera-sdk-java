/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2023 Hedera Hashgraph, LLC
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

import com.hedera.hashgraph.sdk.proto.TokenType;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TokenTypeTest {

    private final TokenType tokenTypeFungible = TokenType.FUNGIBLE_COMMON;
    private final TokenType tokenTypeNonFungible = TokenType.NON_FUNGIBLE_UNIQUE;

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void fromProtobuf() {
        SnapshotMatcher.expect(
                com.hedera.hashgraph.sdk.TokenType.valueOf(tokenTypeFungible).toString(),
                com.hedera.hashgraph.sdk.TokenType.valueOf(tokenTypeNonFungible).toString())
            .toMatchSnapshot();
    }

    @Test
    void toProtobuf() {
        SnapshotMatcher.expect(
                com.hedera.hashgraph.sdk.TokenType.valueOf(tokenTypeFungible).toProtobuf(),
                com.hedera.hashgraph.sdk.TokenType.valueOf(tokenTypeNonFungible).toProtobuf())
            .toMatchSnapshot();
    }
}
