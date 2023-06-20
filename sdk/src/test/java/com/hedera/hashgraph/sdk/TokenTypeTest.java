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
