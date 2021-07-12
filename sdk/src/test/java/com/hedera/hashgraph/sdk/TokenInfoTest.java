package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList;

// TODO: update this, test deepClone()

public class TokenInfoTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    private TokenInfo spawnTokenInfoExample() {
        List<CustomFee> fees = new ArrayList<>();
        fees.add(new CustomFixedFee()
            .setFeeCollectorAccountId(new AccountId(4322))
            .setDenominatingTokenId(new TokenId(483902))
            .setAmount(10));
        fees.add(new CustomFractionalFee()
            .setFeeCollectorAccountId(new AccountId(389042))
            .setNumerator(3)
            .setDenominator(7)
            .setMin(3)
            .setMax(100));
        return new TokenInfo(
            new TokenId(48920),
            "name",
            "&",
            3,
            1000,
            new AccountId(932),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null,
            null,
            "memo",
            fees,
            TokenType.FUNGIBLE_COMMON,
            TokenSupplyType.FINITE,
            1000000
        );
    }

    @Test
    void shouldSerialize() {
        assertDoesNotThrow(() -> {
            var originalTokenInfo = spawnTokenInfoExample();
            byte[] tokenInfoBytes = originalTokenInfo.toBytes();
            var copyTokenInfo = TokenInfo.fromBytes(tokenInfoBytes);
            assertTrue(originalTokenInfo.toString().equals(copyTokenInfo.toString()));
            SnapshotMatcher.expect(originalTokenInfo.toString()).toMatchSnapshot();
        });
    }
}