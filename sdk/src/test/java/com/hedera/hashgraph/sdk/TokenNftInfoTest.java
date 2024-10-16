package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenNftInfoTest {
    final static Instant creationTime = Instant.ofEpochSecond(1554158542);

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
            spenderAccountId
        );
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
