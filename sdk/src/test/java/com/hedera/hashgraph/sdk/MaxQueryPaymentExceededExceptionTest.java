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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaxQueryPaymentExceededExceptionTest {
    @Test
    void shouldHaveMessage() {
        var e = new MaxQueryPaymentExceededException(
            new AccountBalanceQuery(),
            new Hbar(30),
            new Hbar(15)
        );

        assertThat(e.getMessage()).isEqualTo(
            "cost for AccountBalanceQuery, of 30 ℏ, without explicit payment is greater than the maximum allowed payment of 15 ℏ"
        );
    }
}
