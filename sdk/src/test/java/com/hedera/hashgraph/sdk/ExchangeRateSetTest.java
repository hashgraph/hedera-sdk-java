package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

import org.threeten.bp.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExchangeRateSetTest {
    private static final String exchangeRateSetHex = "0a1008b0ea0110b6b4231a0608f0bade9006121008b0ea01108cef231a060880d7de9006";

    @Test
    void fromProtobuf() throws InvalidProtocolBufferException {
        byte[] exchangeRateSetBytes = Hex.decode(exchangeRateSetHex);

        ExchangeRateSet exchangeRateSet = ExchangeRateSet.fromBytes(exchangeRateSetBytes);

        assertEquals(580150, exchangeRateSet.currentRate.cents);
        assertEquals(30000, exchangeRateSet.currentRate.hbars);
        Instant currentExpirationTime = Instant.ofEpochSecond(1645714800);
        assertEquals(currentExpirationTime, exchangeRateSet.currentRate.expirationTime);
        assertEquals(19.338333333333335, exchangeRateSet.currentRate.exchangeRate);

        assertEquals(587660, exchangeRateSet.nextRate.cents);
        assertEquals(30000, exchangeRateSet.nextRate.hbars);
        Instant nextExpirationTime = Instant.ofEpochSecond(1645718400);
        assertEquals(nextExpirationTime, exchangeRateSet.nextRate.expirationTime);
        assertEquals(19.588666666666665, exchangeRateSet.nextRate.exchangeRate);
    }
}
