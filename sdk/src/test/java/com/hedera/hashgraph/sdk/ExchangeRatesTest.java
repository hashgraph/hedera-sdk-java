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
