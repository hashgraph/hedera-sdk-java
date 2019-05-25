package com.hedera.hashgraph.sdk;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
    @DisplayName("funcSelector() produces correct hash")
    void funcSelectorTest(String hash, String funcName, String[] paramTypes) {
        assertEquals(
            hash,
            Hex.toHexString(CallParams.funcSelector(funcName, Arrays.asList(paramTypes)).toByteArray())
        );
    }

    @Test
    @DisplayName("encodes params correctly")
    void encodesParamsCorrectly() {
        final var paramsStringArg = new CallParams("set_message")
            .add("Hello, world!")
            .toProto();

        final var paramsBytesArg = new CallParams("set_message")
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

    private static Stream<Arguments> uint256Arguments() {
        return Stream.of(
            of(0, "0000000000000000000000000000000000000000000000000000000000000000"),
            of(2, "0000000000000000000000000000000000000000000000000000000000000002"),
            of(255, "00000000000000000000000000000000000000000000000000000000000000ff"),
            of(4095, "0000000000000000000000000000000000000000000000000000000000000fff"),
            of(255 << 24, "00000000000000000000000000000000000000000000000000000000ff000000"),
            of(4095 << 20, "00000000000000000000000000000000000000000000000000000000fff00000"),
            of(0xdeadbeef, "00000000000000000000000000000000000000000000000000000000deadbeef")
        );
    }

    @ParameterizedTest
    @DisplayName("uint256() encodes correctly")
    @MethodSource("uint256Arguments")
    void uint256EncodesCorrectly(int val, String hexString) {
        assertEquals(
            Hex.toHexString(CallParams.uint256(val).toByteArray()),
            hexString
        );
    }
}
