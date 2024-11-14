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

import com.google.gson.JsonObject;
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
    void testSetContractIdWithNullThrowsException() {
        assertThrows(NullPointerException.class, () -> query.setContractId(null));
    }

    @Test
    void testSetAndGetContractEvmAddress() {
        String evmAddress = "0x1234567890abcdef1234567890abcdef12345678";
        query.setContractEvmAddress(evmAddress);
        assertEquals(evmAddress, query.getContractEvmAddress());
        assertNull(query.getContractId());
    }

    @Test
    void testSetContractEvmAddressWithNullThrowsException() {
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
        assertNotNull(query.getCallData());
    }

    @Test
    void testSetAndGetBlockNumber() {
        long blockNumber = 123456;
        query.setBlockNumber(blockNumber);
        assertEquals(blockNumber, query.getBlockNumber());
    }

    @Test
    void testSetAndGetValue() {
        long value = 1000;
        query.setValue(value);
        assertEquals(value, query.getValue());
    }

    @Test
    void testSetAndGetGas() {
        long gas = 50000;
        query.setGasLimit(gas);
        assertEquals(gas, query.getGasLimit());
    }

    @Test
    void testSetAndGetGasPrice() {
        long gasPrice = 200;
        query.setGasPrice(gasPrice);
        assertEquals(gasPrice, query.getGasPrice());
    }

    @Test
    void testEstimateGasWithMissingContractIdOrEvmAddressThrowsException() {
        ByteString params = ByteString.copyFromUtf8("gasParams");
        query.setFunctionParameters(params);

        assertThrows(NullPointerException.class, () -> query.estimate(null));
    }

    @Test
    void testCreateJsonPayloadAllFieldsSet() {
        byte[] data = "testData".getBytes();
        String senderAddress = "0x1234567890abcdef1234567890abcdef12345678";
        String contractAddress = "0xabcdefabcdefabcdefabcdefabcdefabcdef";
        long gas = 50000;
        long gasPrice = 2000;
        long value = 1000;
        String blockNumber = "latest";
        boolean estimate = true;

        String jsonPayload = MirrorNodeContractQuery.createJsonPayload(data, senderAddress, contractAddress, gas,
            gasPrice, value, blockNumber, estimate);

        JsonObject expectedJson = new JsonObject();
        expectedJson.addProperty("data", "testData");
        expectedJson.addProperty("to", contractAddress);
        expectedJson.addProperty("estimate", estimate);
        expectedJson.addProperty("blockNumber", blockNumber);
        expectedJson.addProperty("from", senderAddress);
        expectedJson.addProperty("gas", gas);
        expectedJson.addProperty("gasPrice", gasPrice);
        expectedJson.addProperty("value", value);

        assertEquals(expectedJson.toString(), jsonPayload);
    }

    @Test
    void testCreateJsonPayloadOnlyRequiredFieldsSet() {
        byte[] data = "testData".getBytes();
        String senderAddress = "";
        String contractAddress = "0xabcdefabcdefabcdefabcdefabcdefabcdef";
        long gas = 0;
        long gasPrice = 0;
        long value = 0;
        String blockNumber = "latest";
        boolean estimate = true;

        String jsonPayload = MirrorNodeContractQuery.createJsonPayload(data, senderAddress, contractAddress, gas,
            gasPrice, value, blockNumber, estimate);

        JsonObject expectedJson = new JsonObject();
        expectedJson.addProperty("data", "testData");
        expectedJson.addProperty("to", contractAddress);
        expectedJson.addProperty("estimate", estimate);
        expectedJson.addProperty("blockNumber", blockNumber);

        assertEquals(expectedJson.toString(), jsonPayload);
    }

    @Test
    void testCreateJsonPayloadSomeOptionalFieldsSet() {
        byte[] data = "testData".getBytes();
        String senderAddress = "0x1234567890abcdef1234567890abcdef12345678";
        String contractAddress = "0xabcdefabcdefabcdefabcdefabcdefabcdef";
        long gas = 50000;
        long gasPrice = 0;
        long value = 1000;
        String blockNumber = "latest";
        boolean estimate = false;

        String jsonPayload = MirrorNodeContractQuery.createJsonPayload(data, senderAddress, contractAddress, gas,
            gasPrice, value, blockNumber, estimate);

        JsonObject expectedJson = new JsonObject();
        expectedJson.addProperty("data", "testData");
        expectedJson.addProperty("to", contractAddress);
        expectedJson.addProperty("estimate", estimate);
        expectedJson.addProperty("blockNumber", blockNumber);
        expectedJson.addProperty("from", senderAddress);
        expectedJson.addProperty("gas", gas);
        expectedJson.addProperty("value", value);

        assertEquals(expectedJson.toString(), jsonPayload);
    }

    @Test
    void testCreateJsonPayloadAllOptionalFieldsDefault() {
        byte[] data = "testData".getBytes();
        String contractAddress = "0xabcdefabcdefabcdefabcdefabcdefabcdef";
        String senderAddress = "";
        long gas = 0;
        long gasPrice = 0;
        long value = 0;
        String blockNumber = "latest";
        boolean estimate = false;

        String jsonPayload = MirrorNodeContractQuery.createJsonPayload(data, senderAddress, contractAddress, gas,
            gasPrice, value, blockNumber, estimate);

        JsonObject expectedJson = new JsonObject();
        expectedJson.addProperty("data", "testData");
        expectedJson.addProperty("to", contractAddress);
        expectedJson.addProperty("estimate", estimate);
        expectedJson.addProperty("blockNumber", blockNumber);

        assertEquals(expectedJson.toString(), jsonPayload);
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

    @Test
    void shouldSerialize() {
        ContractId testContractId = new ContractId(0, 0, 1234);
        String testEvmAddress = "0x1234567890abcdef1234567890abcdef12345678";
        AccountId testSenderId = new AccountId(0, 0, 5678);
        String testSenderEvmAddress = "0xabcdefabcdefabcdefabcdefabcdefabcdef";
        ByteString testCallData = ByteString.copyFromUtf8("testData");
        String testFunctionName = "myFunction";
        ContractFunctionParameters testParams = new ContractFunctionParameters().addString("param1");

        long testValue = 1000L;
        long testGasLimit = 500000L;
        long testGasPrice = 20L;
        long testBlockNumber = 123456L;

        var query = new MirrorNodeContractQuery()
            .setContractId(testContractId)
            .setContractEvmAddress(testEvmAddress)
            .setSender(testSenderId)
            .setSenderEvmAddress(testSenderEvmAddress)
            .setFunction(testFunctionName, testParams)
            .setFunctionParameters(testCallData)
            .setValue(testValue)
            .setGasLimit(testGasLimit)
            .setGasPrice(testGasPrice)
            .setBlockNumber(testBlockNumber);

        SnapshotMatcher.expect(query.toString()
        ).toMatchSnapshot();
    }
}
