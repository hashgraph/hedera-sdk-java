/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.protobuf.ByteString;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MirrorNodeContractQueryTest {

    private MirrorNodeContractQuery query;
    private ContractId mockContractId;

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @BeforeEach
    void setUp() {
        query = new MirrorNodeContractQuery();
        mockContractId = Mockito.mock(ContractId.class);
    }

    @Test
    void testSetAndGetContractId() {
        query.setContractId(mockContractId);
        assertEquals(mockContractId, query.getContractId());
    }

    @Test
    void testSetContractIdWithNull_ThrowsException() {
        assertThrows(NullPointerException.class, () -> query.setContractId(null));
    }

    @Test
    void testSetAndGetContractEvmAddress() {
        String evmAddress = "0x1234567890abcdef1234567890abcdef12345678";
        query.setContractEvmAddress(evmAddress);
        assertEquals(evmAddress, query.getContractEvmAddress());
        assertNull(query.getContractId(), "Setting EVM address should reset ContractId to null.");
    }

    @Test
    void testSetContractEvmAddressWithNull_ThrowsException() {
        assertThrows(NullPointerException.class, () -> query.setContractEvmAddress(null));
    }

    @Test
    void testSetAndGetcallData() {
        ByteString params = ByteString.copyFromUtf8("test");
        query.setFunctionParameters(params);
        assertArrayEquals(params.toByteArray(), query.getCallData());
    }

    @Test
    void testSetFunctionWithoutParameters() {
        query.setFunction("myFunction");
        assertNotNull(query.getCallData(), "Function parameters should not be null after setting a function.");
    }

    @Test
    void testSetAndGetBlockNumber() {
        long blockNumber = 123456;
        query.setBlockNumber(blockNumber);
        assertEquals(blockNumber, query.getBlockNumber());
    }

    @Test
    void testEstimateGasWithMissingContractIdOrEvmAddress_ThrowsException() {
        ByteString params = ByteString.copyFromUtf8("gasParams");
        query.setFunctionParameters(params);

        assertThrows(NullPointerException.class, () -> query.estimate(null));
    }

    @Test
    void testCreateJsonPayload() {
        String contractAddress = "0xabcdefabcdefabcdefabcdefabcdefabcdef";
        byte[] data = "data".getBytes();
        String jsonPayload = MirrorNodeContractQuery.createJsonPayload(data, contractAddress, "latest", true);

        String expectedJson = """
            {
              "data": "64617461",
              "to": "0xabcdefabcdefabcdefabcdefabcdefabcdef",
              "estimate": true,
              "blockNumber": "latest"
            }
            """;
        assertEquals(expectedJson.trim(), jsonPayload.trim());
    }

    @Test
    void testParseHexEstimateToLong() {
        String responseBody = "{\"result\": \"0x1234\"}";
        long parsedResult = MirrorNodeContractQuery.parseHexEstimateToLong(responseBody);
        assertEquals(0x1234, parsedResult);
    }

    @Test
    void testParseContractCallResult() {
        String responseBody = "{\"result\": \"0x1234abcdef\"}";
        String parsedResult = MirrorNodeContractQuery.parseContractCallResult(responseBody);
        assertEquals("0x1234abcdef", parsedResult);
    }
}
