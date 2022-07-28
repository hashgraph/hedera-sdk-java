package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.bouncycastle.util.encoders.Hex;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenNftInfoTest {
    final static Instant creationTime = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
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
            spenderAccountId
        );
    }

    @Test
    void shouldSerialize() throws Exception {
        var originalTokenInfo = spawnTokenNftInfoExample(AccountId.fromString("8.9.10"));
        byte[] tokenInfoBytes = originalTokenInfo.toBytes();
        var copyTokenInfo = TokenNftInfo.fromBytes(tokenInfoBytes);
        assertEquals(originalTokenInfo.toString(), copyTokenInfo.toString());
        SnapshotMatcher.expect(originalTokenInfo.toString()).toMatchSnapshot();
    }

    @Test
    void shouldSerializeNullSpender() throws Exception {
        var originalTokenInfo = spawnTokenNftInfoExample(null);
        byte[] tokenInfoBytes = originalTokenInfo.toBytes();
        var copyTokenInfo = TokenNftInfo.fromBytes(tokenInfoBytes);
        assertEquals(originalTokenInfo.toString(), copyTokenInfo.toString());
        SnapshotMatcher.expect(originalTokenInfo.toString()).toMatchSnapshot();
    }
}
