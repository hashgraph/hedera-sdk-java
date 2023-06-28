package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeeAssessmentMethodTest {
    @Test
    void feeAssessmentMethodToString() {
        assertThat(FeeAssessmentMethod.EXCLUSIVE)
            .hasToString(FeeAssessmentMethod.valueOf(true).toString());
        assertThat(FeeAssessmentMethod.INCLUSIVE)
            .hasToString(FeeAssessmentMethod.valueOf(false).toString());
    }
}
