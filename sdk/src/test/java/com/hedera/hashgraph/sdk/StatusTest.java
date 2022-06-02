/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
            () -> Status.valueOf(ResponseCodeEnum.UNRECOGNIZED)
        ).withMessage("network returned unrecognized response code; your SDK may be out of date");
    }
}
