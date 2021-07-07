package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.threeten.bp.Instant;

public class TokenFeeScheduleUpdateTransactionTest {
    final Instant validStart = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    private TokenFeeScheduleUpdateTransaction spawnTokenFeeScheduleUpdateTransactionExample() {
        return new TokenFeeScheduleUpdateTransaction()
            .setTokenId(new TokenId(8798))
            .addCustomFee(new CustomFixedFee()
                .setFeeCollectorAccountId(new AccountId(4322))
                .setDenominatingTokenId(new TokenId(483902))
                .setAmount(10))
            .addCustomFee(new CustomFractionalFee()
                .setFeeCollectorAccountId(new AccountId(389042))
                .setNumerator(3)
                .setDenominator(7)
                .setMin(3)
                .setMax(100))
            .setNodeAccountIds(Collections.singletonList(AccountId.fromString("0.0.5005")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .freeze();
    }

    @Test
    void shouldSerialize() {
        assertDoesNotThrow(() -> {
            var originalUpdate = spawnTokenFeeScheduleUpdateTransactionExample();
            byte[] updateBytes = originalUpdate.toBytes();
            var copyUpdate = TokenFeeScheduleUpdateTransaction.fromBytes(updateBytes);
            assertTrue(originalUpdate.toString().equals(copyUpdate.toString()));
            SnapshotMatcher.expect(originalUpdate.toString()).toMatchSnapshot();
        });
    }
}