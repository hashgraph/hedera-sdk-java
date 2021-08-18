package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomFeeListTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    private List<CustomFee> spawnCustomFeeListExample() {
        var returnList = new ArrayList<CustomFee>();
        returnList.add(new CustomFixedFee()
            .setFeeCollectorAccountId(new AccountId(4322))
            .setDenominatingTokenId(new TokenId(483902))
            .setAmount(10)
        );
        returnList.add(new CustomFractionalFee()
            .setFeeCollectorAccountId(new AccountId(389042))
            .setNumerator(3)
            .setDenominator(7)
            .setMin(3)
            .setMax(100)
        );
        return returnList;
    }

    @Test
    void shouldSerialize() {
        assertDoesNotThrow(() -> {
            var originalCustomFeeList = spawnCustomFeeListExample();
            byte[] customFee0Bytes = originalCustomFeeList.get(0).toBytes();
            byte[] customFee1Bytes = originalCustomFeeList.get(1).toBytes();
            var copyCustomFeeList = new ArrayList<CustomFee>();
            copyCustomFeeList.add(CustomFee.fromBytes(customFee0Bytes));
            copyCustomFeeList.add(CustomFee.fromBytes(customFee1Bytes));
            assertTrue(originalCustomFeeList.toString().equals(copyCustomFeeList.toString()));
            SnapshotMatcher.expect(originalCustomFeeList.toString()).toMatchSnapshot();
        });
    }

    @Test
    void deepClone() {
        assertDoesNotThrow(() -> {
            var originalCustomFeeList = spawnCustomFeeListExample();
            var copyCustomFeeList = new ArrayList<CustomFee>();
            for (var fee : originalCustomFeeList) {
                copyCustomFeeList.add(fee.deepClone());
            }
            var originalCustomFeeListString = originalCustomFeeList.toString();
            assertTrue(originalCustomFeeListString.equals(copyCustomFeeList.toString()));

            // modifying clone doesn't affect original
            ((CustomFixedFee) (copyCustomFeeList.get(0))).setDenominatingTokenId(new TokenId(89803));
            assertTrue(originalCustomFeeListString.equals(originalCustomFeeList.toString()));

            SnapshotMatcher.expect(originalCustomFeeList.toString()).toMatchSnapshot();
        });
    }
}
