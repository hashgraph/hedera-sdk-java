package com.hiero.sdk;

import com.hiero.sdk.FeeAssessmentMethod;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeeAssessmentMethodTest {
    @Test
    void feeAssessmentMethodToString() {
        Assertions.assertThat(FeeAssessmentMethod.valueOf(true))
            .hasToString(FeeAssessmentMethod.EXCLUSIVE.toString());
        assertThat(FeeAssessmentMethod.valueOf(false))
            .hasToString(FeeAssessmentMethod.INCLUSIVE.toString());
    }
}
