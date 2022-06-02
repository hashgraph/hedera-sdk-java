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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExchangeRatesTest {
    private static final String exchangeRateSetHex = "0a1008b0ea0110b6b4231a0608f0bade9006121008b0ea01108cef231a060880d7de9006";

    @Test
    void fromProtobuf() throws InvalidProtocolBufferException {
        byte[] exchangeRatesBytes = Hex.decode(exchangeRateSetHex);

        ExchangeRates exchangeRates = ExchangeRates.fromBytes(exchangeRatesBytes);

        assertEquals(580150, exchangeRates.currentRate.cents);
        assertEquals(30000, exchangeRates.currentRate.hbars);
        Instant currentExpirationTime = Instant.ofEpochSecond(1645714800);
        assertEquals(currentExpirationTime, exchangeRates.currentRate.expirationTime);
        assertEquals(19.338333333333335, exchangeRates.currentRate.exchangeRateInCents);

        assertEquals(587660, exchangeRates.nextRate.cents);
        assertEquals(30000, exchangeRates.nextRate.hbars);
        Instant nextExpirationTime = Instant.ofEpochSecond(1645718400);
        assertEquals(nextExpirationTime, exchangeRates.nextRate.expirationTime);
        assertEquals(19.588666666666665, exchangeRates.nextRate.exchangeRateInCents);
    }
}
