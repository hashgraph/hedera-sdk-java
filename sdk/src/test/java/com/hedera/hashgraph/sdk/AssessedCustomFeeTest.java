package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AssessedCustomFeeTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    AssessedCustomFee spawnAssessedCustomFeeExample() {
        return new AssessedCustomFee(
            201,
            TokenId.fromString("1.2.3"),
            AccountId.fromString("4.5.6"),
            List.of(
                AccountId.fromString("0.0.1"),
                AccountId.fromString("0.0.2"),
                AccountId.fromString("0.0.3")
            )
        );
    }

    @Test
    void shouldSerialize() throws Exception {
        var originalAssessedCustomFee = spawnAssessedCustomFeeExample();
        byte[] assessedCustomFeeBytes = originalAssessedCustomFee.toBytes();
        var copyAssessedCustomFee = AssessedCustomFee.fromBytes(assessedCustomFeeBytes);
        assertThat(originalAssessedCustomFee.toString().replaceAll("@[A-Za-z0-9]+", ""))
            .isEqualTo(copyAssessedCustomFee.toString().replaceAll("@[A-Za-z0-9]+", ""));
        SnapshotMatcher.expect(originalAssessedCustomFee.toString().replaceAll("@[A-Za-z0-9]+", "")).toMatchSnapshot();
    }
}
