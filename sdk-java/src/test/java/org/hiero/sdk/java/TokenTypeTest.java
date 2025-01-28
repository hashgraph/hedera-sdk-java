// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.hiero.sdk.java.proto.TokenType;
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
                        org.hiero.sdk.java.TokenType.valueOf(tokenTypeFungible).toString(),
                        org.hiero.sdk.java.TokenType.valueOf(tokenTypeNonFungible)
                                .toString())
                .toMatchSnapshot();
    }

    @Test
    void toProtobuf() {
        SnapshotMatcher.expect(
                        org.hiero.sdk.java.TokenType.valueOf(tokenTypeFungible).toProtobuf(),
                        org.hiero.sdk.java.TokenType.valueOf(tokenTypeNonFungible)
                                .toProtobuf())
                .toMatchSnapshot();
    }
}
