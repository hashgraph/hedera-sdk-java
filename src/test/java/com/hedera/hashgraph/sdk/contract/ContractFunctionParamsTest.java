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
            .addInt32(-65536)
            .addUint64(-65536)
            .addAddress("00112233445566778899aabbccddeeff00112233");

        final String paramsHex = Hex.toHexString(params.toBytes(null).toByteArray());

        assertEquals(
            "0000000000000000000000000000000000000000000000000000000011223344"
                // sign-extended
                + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0000"
                // zero-padded
                + "000000000000000000000000000000000000000000000000ffffffffffff0000"
                + "00000000000000000000000000112233445566778899aabbccddeeff00112233",
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
            .addBool(true)
            .addUint8Array(new byte[]{-1, 127});

        final String paramsHex = Hex.toHexString(params.toBytes("foo").toByteArray());

        assertEquals(
            "6a5bb8f2"
                + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffdeadbeef00"
                + "00000000000000000000000000000000000000000000000000000000000000a0"
                + "00000000000000000000000000000000000000000000000000000000000000e0"
                + "0000000000000000000000000000000000000000000000000000000000000001"
                + "0000000000000000000000000000000000000000000000000000000000000120"
                + "000000000000000000000000000000000000000000000000000000000000000d"
                + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000004"
                + "ffee3f7f00000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000002"
                + "00000000000000000000000000000000000000000000000000000000000000ff"
                + "000000000000000000000000000000000000000000000000000000000000007f",
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
            "025838fc" +
                "0000000000000000000000000000000000000000000000000000000000000060" +
                "00000000000000000000000000000000000000000000000000000000000001a0" +
                "0000000000000000000000000000000000000000000000000000000000000240" +
                "0000000000000000000000000000000000000000000000000000000000000003" +
                "0000000000000000000000000000000000000000000000000000000000000060" +
                "00000000000000000000000000000000000000000000000000000000000000a0" +
                "00000000000000000000000000000000000000000000000000000000000000e0" +
                "0000000000000000000000000000000000000000000000000000000000000005" +
                "68656c6c6f000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000001" +
                "2c00000000000000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000006" +
                "776f726c64210000000000000000000000000000000000000000000000000000" +
                "0000000000000000000000000000000000000000000000000000000000000004" +
                "0000000000000000000000000000000000000000000000000000000000000088" +
                "0000000000000000000000000000000000000000000000000000000000000099" +
                "00000000000000000000000000000000000000000000000000000000000000aa" +
                "00000000000000000000000000000000000000000000000000000000000000bb" +
                "0000000000000000000000000000000000000000000000000000000000000001" +
                "0000000000000000000000000000000000000000000000000000000000001111",
            Hex.toHexString(params.toBytes("foo").toByteArray())
        );
    }

    @Test
    @DisplayName("bytes32[] encodes correctly")
    void fixedBytesArrayEncoding() {
        // each string should be padded to 32 bytes and have no length prefix

        final ContractFunctionParams params = new ContractFunctionParams()
            .addBytes32Array(new byte[][] {
                "Hello".getBytes(StandardCharsets.UTF_8),
                ",".getBytes(StandardCharsets.UTF_8),
                "world!".getBytes(StandardCharsets.UTF_8)
            });

        assertEquals(
            "0000000000000000000000000000000000000000000000000000000000000020" +
                "0000000000000000000000000000000000000000000000000000000000000003" + // length of array
                "48656c6c6f000000000000000000000000000000000000000000000000000000" + // "Hello" UTF-8 encoded
                "2c00000000000000000000000000000000000000000000000000000000000000" + // "," UTF-8 encoded
                "776f726c64210000000000000000000000000000000000000000000000000000", // "world!" UTF-8 encoded
            Hex.toHexString(params.toBytes(null).toByteArray())
        );
    }

    @Test
    @DisplayName("bytes[] encodes correctly")
    void dynBytesArrayEncoding() {
        // result should be the exact same as the strings test below
        final ContractFunctionParams params = new ContractFunctionParams()
            .addBytesArray(new byte[][] {
                "Hello".getBytes(StandardCharsets.UTF_8),
                ",".getBytes(StandardCharsets.UTF_8),
                "world!".getBytes(StandardCharsets.UTF_8)
            });

        assertEquals(
            "0000000000000000000000000000000000000000000000000000000000000020" + // offset to array
                "0000000000000000000000000000000000000000000000000000000000000003" + // length of array
                "0000000000000000000000000000000000000000000000000000000000000060" + // first element offset, relative to beginning of this list (after length)
                "00000000000000000000000000000000000000000000000000000000000000a0" + // second element offset
                "00000000000000000000000000000000000000000000000000000000000000e0" + // third element offset
                "0000000000000000000000000000000000000000000000000000000000000005" + // "Hello".length
                "48656c6c6f000000000000000000000000000000000000000000000000000000" + // "Hello" UTF-8 encoded
                "0000000000000000000000000000000000000000000000000000000000000001" + // ",".length
                "2c00000000000000000000000000000000000000000000000000000000000000" + // "," UTF-8 encoded
                "0000000000000000000000000000000000000000000000000000000000000006" + // "world!".length
                "776f726c64210000000000000000000000000000000000000000000000000000", // "world!" UTF-8 encoded
            Hex.toHexString(params.toBytes(null).toByteArray())
        );
    }

    @Test
    @DisplayName("issue 376: string[] encodes correctly")
    void stringArrayEncoding() {
        final ContractFunctionParams params = new ContractFunctionParams()
            .addStringArray(new String[]{"Hello", ",", "world!"});

        assertEquals(
            "0000000000000000000000000000000000000000000000000000000000000020" + // offset to array
                "0000000000000000000000000000000000000000000000000000000000000003" + // length of array
                "0000000000000000000000000000000000000000000000000000000000000060" + // first element offset, relative to beginning of this list (after length)
                "00000000000000000000000000000000000000000000000000000000000000a0" + // second element offset
                "00000000000000000000000000000000000000000000000000000000000000e0" + // third element offset
                "0000000000000000000000000000000000000000000000000000000000000005" + // "Hello".length
                "48656c6c6f000000000000000000000000000000000000000000000000000000" + // "Hello" UTF-8 encoded
                "0000000000000000000000000000000000000000000000000000000000000001" + // ",".length
                "2c00000000000000000000000000000000000000000000000000000000000000" + // "," UTF-8 encoded
                "0000000000000000000000000000000000000000000000000000000000000006" + // "world!".length
                "776f726c64210000000000000000000000000000000000000000000000000000", // "world!" UTF-8 encoded
            Hex.toHexString(params.toBytes(null).toByteArray())
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

    private static Stream<Arguments> uInt256Arguments() {
        return Stream.of(
            Arguments.of(0, "0000000000000000000000000000000000000000000000000000000000000000", 8),
            Arguments.of(2, "0000000000000000000000000000000000000000000000000000000000000002", 8),
            Arguments.of(255, "00000000000000000000000000000000000000000000000000000000000000ff", 8),
            Arguments.of(4095, "0000000000000000000000000000000000000000000000000000000000000fff", 32),
            Arguments.of(127 << 24, "000000000000000000000000000000000000000000000000000000007f000000", 32),
            Arguments.of(2047 << 20, "000000000000000000000000000000000000000000000000000000007ff00000", 32),
            // deadbeef as an integer literal is negative
            Arguments.of(0xdeadbeef, "00000000000000000000000000000000000000000000000000000000deadbeef", 32),
            Arguments.of(-1, "000000000000000000000000000000000000000000000000ffffffffffffffff", 64),
            Arguments.of(-2, "000000000000000000000000000000000000000000000000fffffffffffffffe", 64),
            Arguments.of(-256, "000000000000000000000000000000000000000000000000ffffffffffffff00", 64),
            Arguments.of(-4096, "000000000000000000000000000000000000000000000000fffffffffffff000", 64),
            Arguments.of(255 << 24, "000000000000000000000000000000000000000000000000ffffffffff000000", 64),
            Arguments.of(4095 << 20, "000000000000000000000000000000000000000000000000fffffffffff00000", 64),
            Arguments.of(0xdeadbeefL, "00000000000000000000000000000000000000000000000000000000deadbeef", 64)
        );
    }

    @ParameterizedTest
    @DisplayName("uint256() encodes correctly")
    @MethodSource("uInt256Arguments")
    void uInt256EncodesCorrectly(long val, String hexString, int bitWidth) {
        assertEquals(
            hexString,
            Hex.toHexString(ContractFunctionParams.uint256(val, bitWidth).toByteArray())
        );
    }
}
