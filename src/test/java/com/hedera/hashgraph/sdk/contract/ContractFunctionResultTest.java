package com.hedera.hashgraph.sdk.contract;

import com.google.protobuf.ByteString;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractFunctionResultTest {

    private static final String callResultHex = ""
        + "00000000000000000000000000000000000000000000000000000000ffffffff"
        + "7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        + "00000000000000000000000011223344556677889900aabbccddeeff00112233"
        + "00000000000000000000000000000000000000000000000000000000000000a0"
        + "00000000000000000000000000000000000000000000000000000000000000e0"
        + "000000000000000000000000000000000000000000000000000000000000000d"
        + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"
        + "0000000000000000000000000000000000000000000000000000000000000014"
        + "48656c6c6f2c20776f726c642c20616761696e21000000000000000000000000";

    private static final byte[] callResult = Hex.decode(callResultHex);

    @Test
    @DisplayName("provides results correctly")
    void providesResultsCorrectly() {
        final ContractFunctionResult result = new ContractFunctionResult(
            com.hedera.hashgraph.proto.ContractFunctionResult.newBuilder()
                .setContractCallResult(ByteString.copyFrom(callResult))
        );

        // interpretation varies based on width
        assertTrue(result.getBool(0));
        assertEquals(-1, result.getInt32(0));
        assertEquals((1L << 32) - 1, result.getInt64(0));
        assertEquals(BigInteger.ONE.shiftLeft(32).subtract(BigInteger.ONE), result.getInt256(0));

        assertEquals(BigInteger.ONE.shiftLeft(255).subtract(BigInteger.ONE), result.getInt256(1));

        assertEquals("11223344556677889900aabbccddeeff00112233", result.getAddress(2));

        assertEquals("Hello, world!", result.getString(3));
        assertEquals("Hello, world, again!", result.getString(4));
    }
}
