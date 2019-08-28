package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.of;

class CallParamsTest {

    private static Stream<Arguments> funcSelectorArgs() {
        return Stream.of(
            funcSelectorArgs("cdcd77c0", "baz", "uint32", "bool"),
            funcSelectorArgs("fce353f6", "bar", "bytes3[2]"),
            funcSelectorArgs("a5643bf2", "sam", "bytes", "bool", "uint256[]"),
            funcSelectorArgs("8be65246", "f", "uint256", "uint32[]", "bytes10", "bytes")
            // omitted ("2289b18c", "g", "uint[][]", "string[]")
            // this is the only one that the hash doesn't match which suggests
            // the documentation is wrong here
        );
    }

    private static Arguments funcSelectorArgs(String hash, String funcName, String... paramTypes) {
        return of(hash, funcName, paramTypes);
    }

    @ParameterizedTest
    @MethodSource("funcSelectorArgs")
    @DisplayName("FunctionSelector produces correct hash")
    void funcSelectorTest(String hash, String funcName, String[] paramTypes) {
        final CallParams.FunctionSelector funcSelector = new CallParams.FunctionSelector(funcName);

        for (final String paramType : paramTypes) {
            funcSelector.addParamType(paramType);
        }

        assertEquals(
            hash,
            Hex.toHexString(funcSelector.finish())
        );
    }

    @Test
    @DisplayName("encodes dynamic params correctly")
    void dynamicParamsEncoding() {
        final ByteString paramsStringArg = CallParams.function("set_message")
            .addString("Hello, world!")
            .toProto();

        final ByteString paramsBytesArg = CallParams.function("set_message")
            .addBytes("Hello, world!".getBytes(StandardCharsets.UTF_8))
            .toProto();

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
        final CallParams<CallParams.Constructor> params = CallParams.constructor()
            .addInt(0x11223344, 32)
            // tests implicit widening
            .addUint(0x44556677, 128)
            .addBytes(new byte[]{0x11, 0x22, 0x33, 0x44}, 4)
            .addAddress("00112233445566778899aabbccddeeff00112233")
            .addFunction("44556677889900aabbccddeeff00112233445566", "aabbccdd");

        final String paramsHex = Hex.toHexString(params.toProto().toByteArray());

        assertEquals(
            "0000000000000000000000000000000000000000000000000000000011223344"
                + "0000000000000000000000000000000000000000000000000000000044556677"
                + "1122334400000000000000000000000000000000000000000000000000000000"
                + "00000000000000000000000000112233445566778899aabbccddeeff00112233"
                + "44556677889900aabbccddeeff00112233445566aabbccdd0000000000000000",
            paramsHex
        );
    }

    @Test
    @DisplayName("encodes mixed static and dynamic params correctly")
    void mixedParamsEncoding() {
        final CallParams<CallParams.Function> params = CallParams.function("foo")
            .addInt(BigInteger.valueOf(0xdeadbeef).shiftLeft(8), 72)
            .addString("Hello, world!")
            .addUint(BigInteger.valueOf(0x77889900).shiftLeft(8), 72)
            .addBytes(new byte[]{-1, -18, 63, 127})
            .addBool(true);

        final String paramsHex = Hex.toHexString(params.toProto().toByteArray());

        assertEquals(
            "cd3bd246"
                + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffdeadbeef00"
                + "00000000000000000000000000000000000000000000000000000000000000a0"
                + "0000000000000000000000000000000000000000000000000000007788990000"
                + "00000000000000000000000000000000000000000000000000000000000000e0"
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
        final CallParams<CallParams.Function> params = CallParams.function("foo")
            .addStringArray(new String[]{"hello", ",", "world!"})
            .addStringArray(new String[]{"lorem", "ipsum", "dolor", "sit", "amet"}, 5)
            .addIntArray(new long[]{0x88, 0x99, 0xAA, 0xBB}, 32)
            .addIntArray(new long[]{0xCC, 0xDD, 0xEE, 0xFF}, 32, 4)
            .addIntArray(new BigInteger[]{BigInteger.valueOf(0x1111)}, 128)
            .addIntArray(new BigInteger[]{BigInteger.valueOf(2222)}, 128, 1)
            .addUintArray(new long[]{0x111, 0x222, 0x333, 0x444}, 256)
            .addUintArray(new long[]{0x555, 0x666}, 64, 2)
            .addUintArray(new BigInteger[]{BigInteger.valueOf(0x777)}, 168)
            .addUintArray(new BigInteger[]{BigInteger.valueOf(0x888)}, 144, 1);

        assertEquals(
            "08712407",
            // just test the function selector because the encoded repr of the above is 6.4kib
            Hex.toHexString(params.toProto().toByteArray()).substring(0, 8)
        );
    }

    @ParameterizedTest
    @DisplayName("integer parameter methods check widths")
    @ValueSource(ints = {-128, -8, 0, 3, 9, 384})
    void integerWidthChecks(int width) {
        final CallParams<CallParams.Constructor> params = CallParams.constructor();
        final String message = "Solidity integer width must be a multiple of 8, "
            + "in the closed range [8, 256]";

        assertEquals(
            message,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addInt(0, width)).getMessage());

        assertEquals(
            message,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addInt(BigInteger.ZERO, width)).getMessage());

        assertEquals(
            message,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addUint(width, 0)).getMessage());

        assertEquals(
            message,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addUint(BigInteger.ZERO, width)).getMessage());
    }

    @Test
    @DisplayName("BigInteger and unsigned checks")
    void bigIntAndUnsignedChecks() {
        final CallParams<CallParams.Constructor> params = CallParams.constructor();

        // allowed values for BigInteger
        params.addInt(BigInteger.ONE.shiftLeft(254), 256);
        params.addInt(BigInteger.ONE.negate().shiftLeft(255), 256);
        params.addUint(BigInteger.ONE.shiftLeft(255), 256);

        final String negativeErr = "addUint() does not accept negative values";

        assertEquals(
            negativeErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addUint(-1, 64)).getMessage());

        assertEquals(
            negativeErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addUint(BigInteger.ONE.negate(), 64)).getMessage());

        final String rangeErr = "BigInteger out of range for Solidity integers";

        assertEquals(
            rangeErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addInt(BigInteger.ONE.shiftLeft(255), 256)).getMessage());

        assertEquals(
            rangeErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addInt(BigInteger.ONE.negate().shiftLeft(256), 256))
                .getMessage());

        assertEquals(
            rangeErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addUint(BigInteger.ONE.shiftLeft(256), 256))
                .getMessage());

        final String widthErr = "BigInteger.bitLength() is greater than the nominal parameter width";

        assertEquals(
            widthErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addInt(BigInteger.ONE.shiftLeft(65), 64)).getMessage());

        assertEquals(
            widthErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addInt(BigInteger.ONE.negate().shiftLeft(65), 64))
                .getMessage());

        assertEquals(
            widthErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addUint(BigInteger.ONE.shiftLeft(65), 64))
                .getMessage());
    }

    @Test
    @DisplayName("address param checks")
    void addressParamChecks() {
        final CallParams<CallParams.Constructor> params = CallParams.constructor();

        final String lenErr = "Solidity addresses must be 20 bytes or 40 hex chars";

        assertEquals(
            lenErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addAddress(new byte[0])).getMessage());

        assertEquals(
            lenErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addAddress(new byte[17])).getMessage());

        assertEquals(
            lenErr,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addAddress(new byte[21])).getMessage());

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

    private static Stream<Arguments> int256Arguments() {
        return Stream.of(
            of(0, "0000000000000000000000000000000000000000000000000000000000000000"),
            of(2, "0000000000000000000000000000000000000000000000000000000000000002"),
            of(255, "00000000000000000000000000000000000000000000000000000000000000ff"),
            of(4095, "0000000000000000000000000000000000000000000000000000000000000fff"),
            of(127 << 24, "000000000000000000000000000000000000000000000000000000007f000000"),
            of(2047 << 20, "000000000000000000000000000000000000000000000000000000007ff00000"),
            // deadbeef as an integer literal is negative
            of(0xdeadbeefL, "00000000000000000000000000000000000000000000000000000000deadbeef"),
            of(-1, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff"),
            of(-2, "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe"),
            of(-256, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00"),
            of(-4096, "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff000"),
            of(255 << 24, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffff000000"),
            of(4095 << 20, "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00000"),
            of(0xdeadbeef, "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffdeadbeef")
        );
    }

    @ParameterizedTest
    @DisplayName("int256() encodes correctly")
    @MethodSource("int256Arguments")
    void int256EncodesCorrectly(long val, String hexString) {
        assertEquals(
            hexString,
            Hex.toHexString(CallParams.int256(val, 64).toByteArray())
        );
    }
}
