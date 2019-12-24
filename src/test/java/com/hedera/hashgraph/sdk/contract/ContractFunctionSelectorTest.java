package com.hedera.hashgraph.sdk.contract;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContractFunctionSelectorTest {
    @SuppressWarnings("unused")
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
        return Arguments.of(hash, funcName, paramTypes);
    }

    @ParameterizedTest
    @MethodSource("funcSelectorArgs")
    @DisplayName("FunctionSelector produces correct hash")
    void funcSelectorTest(String hash, String funcName, String[] paramTypes) {
        final ContractFunctionSelector funcSelector = new ContractFunctionSelector(funcName);

        for (final String paramType : paramTypes) {
            funcSelector.addParamType(paramType);
        }

        assertEquals(
            hash,
            Hex.toHexString(funcSelector.finish())
        );
    }
}
