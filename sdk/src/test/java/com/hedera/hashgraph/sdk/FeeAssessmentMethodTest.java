package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FeeAssessmentMethodTest {
    @Test
    void feeAssessmentMethodToString() {
        assertThat(FeeAssessmentMethod.EXCLUSIVE.toString())
            .isEqualTo(FeeAssessmentMethod.valueOf(true).toString());
        assertThat(FeeAssessmentMethod.INCLUSIVE.toString())
            .isEqualTo(FeeAssessmentMethod.valueOf(false).toString());
    }
}
