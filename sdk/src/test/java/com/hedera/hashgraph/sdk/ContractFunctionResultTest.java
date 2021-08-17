package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ContractFunctionResultTest {
    private static final String CALL_RESULT_HEX = ""
        + "00000000000000000000000000000000000000000000000000000000ffffffff"
        + "7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        + "00000000000000000000000011223344556677889900aabbccddeeff00112233"
        + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"
        + "00000000000000000000000000000000000000000000000000000000000000c0"
        + "0000000000000000000000000000000000000000000000000000000000000100"
        + "000000000000000000000000000000000000000000000000000000000000000d"
        + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"
        + "0000000000000000000000000000000000000000000000000000000000000014"
        + "48656c6c6f2c20776f726c642c20616761696e21000000000000000000000000";

    private static final String STRING_ARRAY_RESULT_HEX = ""
        + "0000000000000000000000000000000000000000000000000000000000000020"
        + "0000000000000000000000000000000000000000000000000000000000000002"
        + "0000000000000000000000000000000000000000000000000000000000000040"
        + "0000000000000000000000000000000000000000000000000000000000000080"
        + "000000000000000000000000000000000000000000000000000000000000000C"
        + "72616E646F6D2062797465730000000000000000000000000000000000000000"
        + "000000000000000000000000000000000000000000000000000000000000000C"
        + "72616E646F6D2062797465730000000000000000000000000000000000000000";

    private static final byte[] callResult = Hex.decode(CALL_RESULT_HEX);
    private static final byte[] stringArrayCallResult = Hex.decode(STRING_ARRAY_RESULT_HEX);

    @Test
    @DisplayName("provides results correctly")
    void providesResultsCorrectly() {
        var result = new ContractFunctionResult(
            com.hedera.hashgraph.sdk.proto.ContractFunctionResult.newBuilder()
                .setContractCallResult(ByteString.copyFrom(callResult))
        );

        // interpretation varies based on width
        assertTrue(result.getBool(0));
        assertEquals(-1, result.getInt32(0));
        assertEquals((1L << 32) - 1, result.getInt64(0));
        assertEquals(BigInteger.ONE.shiftLeft(32).subtract(BigInteger.ONE), result.getInt256(0));

        assertEquals(BigInteger.ONE.shiftLeft(255).subtract(BigInteger.ONE), result.getInt256(1));

        assertEquals("11223344556677889900aabbccddeeff00112233", result.getAddress(2));

        // unsigned integers (where applicable)
        assertEquals(-1, result.getUint32(3));
        assertEquals(-1L, result.getUint64(3));
        // BigInteger can represent the full range and so should be 2^256 - 1
        assertEquals(BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE), result.getUint256(3));

        assertEquals("Hello, world!", result.getString(4));
        assertEquals("Hello, world, again!", result.getString(5));
    }

    @Test
    @DisplayName("can get string array reuslt")
    void canGetStringArrayResult() {
        var result = new ContractFunctionResult(
            com.hedera.hashgraph.sdk.proto.ContractFunctionResult.newBuilder()
                .setContractCallResult(ByteString.copyFrom(stringArrayCallResult))
        );

        var strings = result.getStringArray(0);
        assertEquals(strings.get(0), "random bytes");
        assertEquals(strings.get(1), "random bytes");
    }
}
