package com.hedera.hashgraph.sdk.contract;

import com.google.protobuf.ByteString;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContractFunctionParamsTest {
    @Test
    @DisplayName("encodes dynamic params correctly")
    void dynamicParamsEncoding() {
        final ByteString paramsStringArg = new ContractFunctionParams()
            .addString("Hello, world!")
            .toBytes("set_message");

        final ByteString paramsBytesArg = new ContractFunctionParams()
            .addBytes("Hello, world!".getBytes(StandardCharsets.UTF_8))
            .toBytes("set_message");

        final String paramsStringArgHex = Hex.toHexString(paramsStringArg.toByteArray());
        final String paramsBytesArgHex = Hex.toHexString(paramsBytesArg.toByteArray());

        assertEquals(
            "2e982602"
                + "0000000000000000000000000000000000000000000000000000000000000020"
                + "000000000000000000000000000000000000000000000000000000000000000d"
                + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000",
            paramsStringArgHex);

        // signature should encode differently but the contents are identical
        assertEquals(
            "010473a7"
                + "0000000000000000000000000000000000000000000000000000000000000020"
                + "000000000000000000000000000000000000000000000000000000000000000d"
                + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000",
            paramsBytesArgHex);
    }

    @Test
    @DisplayName("encodes static params correctly")
    void staticParamsEncoding() {
        final ContractFunctionParams params = new ContractFunctionParams()
            .addInt32(0x11223344)
            .addBytes(new byte[]{0x11, 0x22, 0x33, 0x44})
            .addAddress("00112233445566778899aabbccddeeff00112233");

        final String paramsHex = Hex.toHexString(params.toBytes(null).toByteArray());

        assertEquals(
            "0000000000000000000000000000000000000000000000000000000011223344"
                + "0000000000000000000000000000000000000000000000000000000000000060"
                + "00112233445566778899aabbccddeeff00112233000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000004"
                + "1122334400000000000000000000000000000000000000000000000000000000",
            paramsHex
        );
    }

    @Test
    @DisplayName("encodes mixed static and dynamic params correctly")
    void mixedParamsEncoding() {
        final ContractFunctionParams params = new ContractFunctionParams()
            .addInt256(BigInteger.valueOf(0xdeadbeef).shiftLeft(8))
            .addString("Hello, world!")
            .addBytes(new byte[]{-1, -18, 63, 127})
            .addBool(true);

        final String paramsHex = Hex.toHexString(params.toBytes("foo").toByteArray());

        assertEquals(
            "be0d49d2"
                + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffdeadbeef00"
                + "0000000000000000000000000000000000000000000000000000000000000080"
                + "00000000000000000000000000000000000000000000000000000000000000c0"
                + "0000000000000000000000000000000000000000000000000000000000000001"
                + "000000000000000000000000000000000000000000000000000000000000000d"
                + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000004"
                + "ffee3f7f00000000000000000000000000000000000000000000000000000000",
            paramsHex
        );
    }

    @Test
    @DisplayName("encodes array types correctly")
    void arrayTypesEncoding() {
        final ContractFunctionParams params = new ContractFunctionParams()
            .addStringArray(new String[]{"hello", ",", "world!"})
            .addInt32Array(new int[]{0x88, 0x99, 0xAA, 0xBB})
            .addInt256Array(new BigInteger[]{BigInteger.valueOf(0x1111)});

        assertEquals(
            "025838fc",
            Hex.toHexString(params.toBytes("foo").toByteArray()).substring(0, 8)
        );
    }

    @Test
    @DisplayName("BigInteger checks")
    void bigIntChecks() {
        final ContractFunctionParams params = new ContractFunctionParams();

        // allowed values for BigInteger
        params.addInt256(BigInteger.ONE.shiftLeft(254));
        params.addInt256(BigInteger.ONE.negate().shiftLeft(255));

        final String negativeErr = "addUint() does not accept negative values";
        final String rangeErr = "BigInteger out of range for Solidity integers";

        assertEquals(
            rangeErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addInt256(BigInteger.ONE.shiftLeft(255))).getMessage());

        assertEquals(
            rangeErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addInt256(BigInteger.ONE.negate().shiftLeft(256)))
                .getMessage());
    }

    @Test
    @DisplayName("address param checks")
    void addressParamChecks() {
        final ContractFunctionParams params = new ContractFunctionParams();

        final String lenErr = "Solidity addresses must be 40 hex chars";
        assertEquals(
            lenErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addAddress("")).getMessage());

        assertEquals(
            lenErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addAddress("aabbccdd")).getMessage());

        assertEquals(
            lenErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addAddress("00112233445566778899aabbccddeeff0011223344"))
                .getMessage());

        assertEquals(
            "failed to decode Solidity address as hex",
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addAddress("gghhii--__zz66778899aabbccddeeff00112233"))
                .getMessage());
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> int256Arguments() {
        return Stream.of(
            Arguments.of(0, "0000000000000000000000000000000000000000000000000000000000000000"),
            Arguments.of(2, "0000000000000000000000000000000000000000000000000000000000000002"),
            Arguments.of(255, "00000000000000000000000000000000000000000000000000000000000000ff"),
            Arguments.of(4095, "0000000000000000000000000000000000000000000000000000000000000fff"),
            Arguments.of(127 << 24, "000000000000000000000000000000000000000000000000000000007f000000"),
            Arguments.of(2047 << 20, "000000000000000000000000000000000000000000000000000000007ff00000"),
            // deadbeef as an integer literal is negative
            Arguments.of(0xdeadbeefL, "00000000000000000000000000000000000000000000000000000000deadbeef"),
            Arguments.of(-1, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
            Arguments.of(-2, "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe"),
            Arguments.of(-256, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00"),
            Arguments.of(-4096, "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff000"),
            Arguments.of(255 << 24, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffff000000"),
            Arguments.of(4095 << 20, "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00000"),
            Arguments.of(0xdeadbeef, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffdeadbeef")
        );
    }

    @ParameterizedTest
    @DisplayName("int256() encodes correctly")
    @MethodSource("int256Arguments")
    void int256EncodesCorrectly(long val, String hexString) {
        assertEquals(
            hexString,
            Hex.toHexString(ContractFunctionParams.int256(val, 64).toByteArray())
        );
    }
}
