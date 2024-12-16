// SPDX-License-Identifier: Apache-2.0
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
