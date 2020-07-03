package com.hedera.hashgraph.sdk;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContractFunctionSelectorTest {
    @Test
    void selector() {
        var signature = new ContractFunctionSelector("testFunction")
            .addAddress()
            .addAddressArray()
            .addBool()
            .addBytes()
            .addBytes32()
            .addBytes32Array()
            .addBytesArray()
            .addFunction()
            .addInt8()
            .addInt8Array()
            .addInt32()
            .addInt32Array()
            .addInt64()
            .addInt64Array()
            .addInt256()
            .addInt256Array()
            .addUint8()
            .addUint8Array()
            .addUint32()
            .addUint32Array()
            .addUint64()
            .addUint64Array()
            .addUint256()
            .addUint256Array()
            .addString()
            .addStringArray()
            .finish();

        assertEquals("4438e4ce", Hex.toHexString(signature));
    }
}
