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

import static com.hedera.hashgraph.sdk.EntityIdHelper.getContractAddressFromMirrorNodeAsync;
import static com.hedera.hashgraph.sdk.EntityIdHelper.performQueryToMirrorNodeAsync;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import org.bouncycastle.util.encoders.Hex;

/**
 * MirrorNodeContractQuery returns a result from EVM execution such as cost-free execution of read-only smart contract
 * queries, gas estimation, and transient simulation of read-write operations.
 */
public class MirrorNodeContractQuery {
    private ContractId contractId = null;
    private String contractEvmAddress = null;
    private byte[] callData;
    private long blockNumber;

    public ContractId getContractId() {
        return this.contractId;
    }

    /**
     * Sets the contract instance to call.
     *
     * @param contractId The ContractId to be set
     * @return {@code this}
     */
    public MirrorNodeContractQuery setContractId(ContractId contractId) {
        Objects.requireNonNull(contractId);
        this.contractId = contractId;
        return this;
    }

    public String getContractEvmAddress() {
        return this.contractEvmAddress;
    }

    /**
     * Set the 20-byte EVM address of the contract to call.
     *
     * @param contractEvmAddress
     * @return {@code this}
     */
    public MirrorNodeContractQuery setContractEvmAddress(String contractEvmAddress) {
        Objects.requireNonNull(contractEvmAddress);
        this.contractEvmAddress = contractEvmAddress;
        this.contractId = null;
        return this;
    }

    public byte[] getCallData() {
        return this.callData;
    }

    /**
     * Sets the function to call, and the parameters to pass to the function.
     *
     * @param name   The String to be set as the function name
     * @param params The function parameters to be set
     * @return {@code this}
     */
    public MirrorNodeContractQuery setFunction(String name, ContractFunctionParameters params) {
        Objects.requireNonNull(params);
        return setFunctionParameters(params.toBytes(name));
    }

    /**
     * Sets the function name to call.
     * <p>
     * The function will be called with no parameters. Use {@link #setFunction(String, ContractFunctionParameters)} to
     * call a function with parameters.
     *
     * @param name The String to be set as the function name
     * @return {@code this}
     */
    public MirrorNodeContractQuery setFunction(String name) {
        return setFunction(name, new ContractFunctionParameters());
    }

    /**
     * Sets the function parameters as their raw bytes.
     * <p>
     * Use this instead of {@link #setFunction(String, ContractFunctionParameters)} if you have already pre-encoded a
     * solidity function call.
     *
     * @param functionParameters The function parameters to be set
     * @return {@code this}
     */
    public MirrorNodeContractQuery setFunctionParameters(ByteString functionParameters) {
        Objects.requireNonNull(functionParameters);
        this.callData = functionParameters.toByteArray();
        return this;
    }

    public long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(long blockNumber) {
        this.blockNumber = blockNumber;
    }

    /**
     * Returns gas estimation for the EVM execution
     *
     * @param client
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public long estimate(Client client) throws ExecutionException, InterruptedException {
        if (this.contractEvmAddress == null) {
            Objects.requireNonNull(this.contractId);
            this.contractEvmAddress = getContractAddressFromMirrorNodeAsync(client, this.contractId.toString()).get();
        }
        return getEstimateGasFromMirrorNodeAsync(client, this.callData, this.contractEvmAddress).get();
    }

    /**
     * Does transient simulation of read-write operations and returns the result in hexadecimal string format
     *
     * @param client
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public String call(Client client) throws ExecutionException, InterruptedException {
        if (this.contractEvmAddress == null) {
            Objects.requireNonNull(this.contractId);
            this.contractEvmAddress = getContractAddressFromMirrorNodeAsync(client, this.contractId.toString()).get();
        }

        var blockNum = this.blockNumber == 0 ? "" : String.valueOf(this.blockNumber);
        return getContractCallResultFromMirrorNodeAsync(client, this.callData, this.contractEvmAddress,
            blockNum).get();
    }

    private static CompletableFuture<String> getContractCallResultFromMirrorNodeAsync(Client client, byte[] data,
        String contractAddress, String blockNumber) {
        String apiEndpoint = "/contracts/call";
        String jsonPayload = createJsonPayload(data, contractAddress, blockNumber, false);
        return performQueryToMirrorNodeAsync(client, apiEndpoint, jsonPayload, true)
            .exceptionally(ex -> {
                client.getLogger().error("Error in while performing post request to Mirror Node: " + ex.getMessage());
                throw new CompletionException(ex);
            })
            .thenApply(MirrorNodeContractQuery::parseContractCallResult);
    }

    public static CompletableFuture<Long> getEstimateGasFromMirrorNodeAsync(Client client, byte[] data,
        String contractAddress) {
        String apiEndpoint = "/contracts/call";
        String jsonPayload = createJsonPayload(data, contractAddress, "latest", true);
        return performQueryToMirrorNodeAsync(client, apiEndpoint, jsonPayload, true)
            .exceptionally(ex -> {
                client.getLogger().error("Error in while performing post request to Mirror Node: " + ex.getMessage());
                throw new CompletionException(ex);
            })
            .thenApply(MirrorNodeContractQuery::parseHexEstimateToLong);
    }

    static String createJsonPayload(byte[] data, String contractAddress, String blockNumber, boolean estimate) {
        String hexData = Hex.toHexString(data);
        return String.format("""
            {
              "data": "%s",
              "to": "%s",
              "estimate": %b,
              "blockNumber": "%s"
            }
            """, hexData, contractAddress, estimate, blockNumber);
    }

    static String parseContractCallResult(String responseBody) {
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        return jsonObject.get("result").getAsString();
    }

    static long parseHexEstimateToLong(String responseBody) {
        return Integer.parseInt(parseContractCallResult(responseBody).substring(2), 16);
    }
}
