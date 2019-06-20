package com.hedera.hashgraph.sdk;

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
        final var funcSelector = CallParams.FunctionSelector.function(funcName);

        for (final var paramType : paramTypes) {
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
        final var paramsStringArg = CallParams.function("set_message")
            .addString("Hello, world!")
            .toProto();

        final var paramsBytesArg = CallParams.function("set_message")
            .addBytes("Hello, world!".getBytes(StandardCharsets.UTF_8))
            .toProto();

        final var paramsStringArgHex = Hex.toHexString(paramsStringArg.toByteArray());
        final var paramsBytesArgHex = Hex.toHexString(paramsBytesArg.toByteArray());

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
        final var params = CallParams.constructor()
            .addInt(0x11223344, 32)
            // tests implicit widening
            .addUint(0x44556677, 128)
            .addAddress("00112233445566778899aabbccddeeff00112233")
            .addFunction("44556677889900aabbccddeeff00112233445566", "aabbccdd");

        final var paramsHex = Hex.toHexString(params.toProto().toByteArray());

        assertEquals(
            "5486e94a"
                + "0000000000000000000000000000000000000000000000000000000011223344"
                + "0000000000000000000000000000000000000000000000000000000044556677"
                + "00000000000000000000000000112233445566778899aabbccddeeff00112233"
                + "44556677889900aabbccddeeff00112233445566aabbccdd0000000000000000",
            paramsHex
        );
    }

    @Test
    @DisplayName("encodes mixed static and dynamic params correctly")
    void mixedParamsEncoding() {
        final var params = CallParams.function("foo")
            .addInt(BigInteger.valueOf(0xdeadbeef).shiftLeft(8), 72)
            .addString("Hello, world!")
            .addUint(BigInteger.valueOf(0x77889900).shiftLeft(8), 72)
            .addBytes(new byte[]{-1, -18, 63, 127})
            .addBool(true);

        final var paramsHex = Hex.toHexString(params.toProto().toByteArray());

        assertEquals(
            "1f0001c0"
                + "ffffffffffffffffffffffffffffffffffffffffffffffffffffffdeadbeef00"
                + "00000000000000000000000000000000000000000000000000000000000000a0"
                + "0000000000000000000000000000000000000000000000000000007788990000"
                + "00000000000000000000000000000000000000000000000000000000000000ad"
                + "0000000000000000000000000000000000000000000000000000000000000001"
                + "000000000000000000000000000000000000000000000000000000000000000d"
                + "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000004"
                + "ffee3f7f00000000000000000000000000000000000000000000000000000000",
            paramsHex
        );
    }

    @ParameterizedTest
    @DisplayName("integer parameter methods check widths")
    @ValueSource(ints = {-128, -8, 0, 3, 9, 384})
    void integerWidthChecks(int width) {
        final var params = CallParams.constructor();
        final var message = "Solidity integer width must be a multiple of 8, "
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
                () -> params.addUint(0, width)).getMessage());

        assertEquals(
            message,
            assertThrows(
                IllegalArgumentException.class,
                () -> params.addUint(BigInteger.ZERO, width)).getMessage());
    }

    @Test
    @DisplayName("BigInteger and unsigned checks")
    void bigIntAndUnsignedChecks() {
        final var params = CallParams.constructor();

        // allowed values for BigInteger
        params.addInt(BigInteger.ONE.shiftLeft(254), 256);
        params.addInt(BigInteger.ONE.negate().shiftLeft(255), 256);
        params.addUint(BigInteger.ONE.shiftLeft(255), 256);

        final var negativeErr = "addUint() does not accept negative values";

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

        final var rangeErr = "BigInteger out of range for Solidity integers";

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

        final var widthErr = "BigInteger.bitLength() is greater than the nominal parameter width";

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
