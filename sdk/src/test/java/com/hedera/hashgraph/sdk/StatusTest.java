// SPDX-License-Identifier: Apache-2.0
package com.hedera.hashgraph.sdk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StatusTest {
    @Test
    @DisplayName("Status can be constructed from any ResponseCode")
    void statusToResponseCode() {
        for (ResponseCodeEnum code : ResponseCodeEnum.values()) {
            // not an actual value we want to handle
            // this is what we're given if an unexpected value was decoded
            if (code == ResponseCodeEnum.UNRECOGNIZED) {
                continue;
            }

            Status status = Status.valueOf(code);

            assertThat(code.getNumber()).isEqualTo(status.code.getNumber());
        }
    }

    @Test
    @DisplayName("Status throws on Unrecognized")
    void statusUnrecognized() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> Status.valueOf(ResponseCodeEnum.UNRECOGNIZED))
                .withMessage("network returned unrecognized response code; your SDK may be out of date");
    }
}
