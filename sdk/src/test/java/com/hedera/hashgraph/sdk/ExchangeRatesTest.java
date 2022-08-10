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

import com.google.protobuf.InvalidProtocolBufferException;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

import org.threeten.bp.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class ExchangeRatesTest {
    private static final String exchangeRateSetHex = "0a1008b0ea0110b6b4231a0608f0bade9006121008b0ea01108cef231a060880d7de9006";

    @Test
    void fromProtobuf() throws InvalidProtocolBufferException {
        byte[] exchangeRatesBytes = Hex.decode(exchangeRateSetHex);

        ExchangeRates exchangeRates = ExchangeRates.fromBytes(exchangeRatesBytes);

        assertThat(exchangeRates.currentRate.cents).isEqualTo(580150);
        assertThat(exchangeRates.currentRate.hbars).isEqualTo(30000);
        Instant currentExpirationTime = Instant.ofEpochSecond(1645714800);
        assertThat(exchangeRates.currentRate.expirationTime).isEqualTo(currentExpirationTime);
        assertThat(exchangeRates.currentRate.exchangeRateInCents).isEqualTo(19.338333333333335);

        assertThat(exchangeRates.nextRate.cents).isEqualTo(587660);
        assertThat(exchangeRates.nextRate.hbars).isEqualTo(30000);
        Instant nextExpirationTime = Instant.ofEpochSecond(1645718400);
        assertThat(exchangeRates.nextRate.expirationTime).isEqualTo(nextExpirationTime);
        assertThat(exchangeRates.nextRate.exchangeRateInCents).isEqualTo(19.588666666666665);
    }
}
