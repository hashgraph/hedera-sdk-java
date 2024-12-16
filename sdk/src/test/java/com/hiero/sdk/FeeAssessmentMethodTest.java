// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FeeAssessmentMethodTest {
    @Test
    void feeAssessmentMethodToString() {
        Assertions.assertThat(FeeAssessmentMethod.valueOf(true)).hasToString(FeeAssessmentMethod.EXCLUSIVE.toString());
        assertThat(FeeAssessmentMethod.valueOf(false)).hasToString(FeeAssessmentMethod.INCLUSIVE.toString());
    }
}
