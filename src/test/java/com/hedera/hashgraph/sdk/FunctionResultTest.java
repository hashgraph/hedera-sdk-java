package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ContractFunctionResult;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FunctionResultTest {

    private static final String callResultHex = "" +
        "00000000000000000000000000000000000000000000000000000000ffffffff" +
        "0000000000000000000000000000000000000000000000000000000000000060" +
        "00000000000000000000000000000000000000000000000000000000000000a0" +
        "000000000000000000000000000000000000000000000000000000000000000d" +
        "48656c6c6f2c20776f726c642100000000000000000000000000000000000000" +
        "0000000000000000000000000000000000000000000000000000000000000014" +
        "48656c6c6f2c20776f726c642c20616761696e21000000000000000000000000";

    private static final byte[] callResult = Hex.decode(callResultHex);

    @Test
    @DisplayName("provides results correctly")
    void providesResultsCorrectly() {
        final var result = new FunctionResult(
            ContractFunctionResult.newBuilder()
                .setContractCallResult(ByteString.copyFrom(callResult))
        );

        assertEquals(result.getInt(0), -1);
        assertEquals(result.getString(1), "Hello, world!");
        assertEquals(result.getString(2), "Hello, world, again!");
    }
}
