package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeeAssessmentMethodTest {
    @Test
    void feeAssessmentMethodToString() {
        assertThat(FeeAssessmentMethod.valueOf(true))
            .hasToString(FeeAssessmentMethod.EXCLUSIVE.toString());
        assertThat(FeeAssessmentMethod.valueOf(false))
            .hasToString(FeeAssessmentMethod.INCLUSIVE.toString());
    }
}
