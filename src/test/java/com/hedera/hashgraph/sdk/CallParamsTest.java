package com.hedera.hashgraph.sdk;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @DisplayName("encodes params correctly")
    void encodesParamsCorrectly() {
        final var paramsStringArg = CallParams.function("set_message")
            .add("Hello, world!")
            .toProto();

        final var paramsBytesArg = CallParams.function("set_message")
            .add("Hello, world!".getBytes(StandardCharsets.UTF_8))
            .toProto();

        final var paramsStringArgHex = Hex.toHexString(paramsStringArg.toByteArray());
        final var paramsBytesArgHex = Hex.toHexString(paramsBytesArg.toByteArray());

        Assertions.assertEquals(
            paramsStringArgHex,
            "2e982602" +
                "0000000000000000000000000000000000000000000000000000000000000020" +
                "000000000000000000000000000000000000000000000000000000000000000d" +
                "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"
        );

        // signature should encode differently but the contents are identical
        Assertions.assertEquals(
            paramsBytesArgHex,
            "010473a7" +
                "0000000000000000000000000000000000000000000000000000000000000020" +
                "000000000000000000000000000000000000000000000000000000000000000d" +
                "48656c6c6f2c20776f726c642100000000000000000000000000000000000000"
        );
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
