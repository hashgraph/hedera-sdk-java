package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CustomFeeListTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    private CustomFeeList spawnCustomFeeListExample() {
        return new CustomFeeList()
            .setCanUpdate(true)
            .addCustomFee(new CustomFixedFee()
                .setFeeCollectorAccountId(new AccountId(4322))
                .setDenominatingTokenId(new TokenId(483902))
                .setAmount(10)
            )
            .addCustomFee(new CustomFractionalFee()
                .setFeeCollectorAccountId(new AccountId(389042))
                .setNumerator(3)
                .setDenominator(7)
                .setMin(3)
                .setMax(100)
            );
    }

    @Test
    void shouldSerialize() {
        assertDoesNotThrow(() -> {
            var originalCustomFeeList = spawnCustomFeeListExample();
            byte[] customFeeListBytes = originalCustomFeeList.toBytes();
            var copyCustomFeeList = CustomFeeList.fromBytes(customFeeListBytes);
            assertTrue(originalCustomFeeList.toString().equals(copyCustomFeeList.toString()));
            SnapshotMatcher.expect(originalCustomFeeList.toString()).toMatchSnapshot();
        });
    }
}