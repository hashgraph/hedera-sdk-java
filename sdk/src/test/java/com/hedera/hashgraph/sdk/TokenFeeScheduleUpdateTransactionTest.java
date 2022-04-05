package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private TokenFeeScheduleUpdateTransaction spawnTestTransaction() {
        var customFees = new ArrayList<CustomFee>();
        customFees.add(new CustomFixedFee()
            .setFeeCollectorAccountId(new AccountId(4322))
            .setDenominatingTokenId(new TokenId(483902))
            .setAmount(10)
        );
        customFees.add(new CustomFractionalFee()
            .setFeeCollectorAccountId(new AccountId(389042))
            .setNumerator(3)
            .setDenominator(7)
            .setMin(3)
            .setMax(100)
            .setAssessmentMethod(FeeAssessmentMethod.EXCLUSIVE)
        );

        return new TokenFeeScheduleUpdateTransaction()
            .setTokenId(new TokenId(8798))
            .setCustomFees(customFees)
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .freeze();
    }

    @Test
    void shouldSerialize() {
        assertDoesNotThrow(() -> {
            var originalUpdate = spawnTestTransaction();
            byte[] updateBytes = originalUpdate.toBytes();
            var copyUpdate = TokenFeeScheduleUpdateTransaction.fromBytes(updateBytes);
            assertTrue(originalUpdate.toString().equals(copyUpdate.toString()));
            SnapshotMatcher.expect(originalUpdate.toString()).toMatchSnapshot();
        });
    }
}
