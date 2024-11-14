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
import static com.hedera.hashgraph.sdk.EntityIdHelper.getEvmAddressFromMirrorNodeAsync;
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
    // The contract we are sending the transaction to
    private ContractId contractId = null;
    private String contractEvmAddress = null;
    // The account we are sending the transaction from
    private AccountId sender = null;
    private String senderEvmAddress = null;
    // The transaction callData
    private byte[] callData;
    // The amount we are sending to payable functions
    private long value;
    // The gas limit
    private long gasLimit;
    // The gas price
    private long gasPrice;
    // The block number for the simulation
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

    public AccountId getSender() {
        return this.sender;
    }

    /**
     * Sets the sender of the transaction simulation.
     *
     * @param sender The AccountId to be set
     * @return {@code this}
     */
    public MirrorNodeContractQuery setSender(AccountId sender) {
        Objects.requireNonNull(sender);
        this.sender = sender;
        return this;
    }

    public String getSenderEvmAddress() {
        return this.senderEvmAddress;
    }

    /**
     * Set the 20-byte EVM address of the sender.
     *
     * @param senderEvmAddress
     * @return {@code this}
     */
    public MirrorNodeContractQuery setSenderEvmAddress(String senderEvmAddress) {
        Objects.requireNonNull(senderEvmAddress);
        this.senderEvmAddress = senderEvmAddress;
        this.sender = null;
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

    public long getValue() {
        return this.value;
    }

    public MirrorNodeContractQuery setValue(long value) {
        this.value = value;
        return this;
    }

    public long getGasLimit() {
        return this.gasLimit;
    }

    public MirrorNodeContractQuery setGasLimit(long gasLimit) {
        this.gasLimit = gasLimit;
        return this;
    }

    public long getGasPrice() {
        return gasPrice;
    }

    public MirrorNodeContractQuery setGasPrice(long gasPrice) {
        this.gasPrice = gasPrice;
        return this;
    }

    public long getBlockNumber() {
        return this.blockNumber;
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
        fillEvmAddresses(client);
        return getEstimateGasFromMirrorNodeAsync(client).get();
    }

    /**
     * Does transient simulation of read-write operations and returns the result in hexadecimal string format. The
     * result can be any solidity type.
     *
     * @param client
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public String call(Client client) throws ExecutionException, InterruptedException {
        fillEvmAddresses(client);
        var blockNum = this.blockNumber == 0 ? "latest" : String.valueOf(this.blockNumber);
        return getContractCallResultFromMirrorNodeAsync(client, blockNum).get();
    }

    private void fillEvmAddresses(Client client) throws ExecutionException, InterruptedException {
        if (this.contractEvmAddress == null) {
            Objects.requireNonNull(this.contractId);
            this.contractEvmAddress = getContractAddressFromMirrorNodeAsync(client, this.contractId.toString()).get();
        }

        if (this.senderEvmAddress == null && this.sender != null) {
            this.senderEvmAddress = getEvmAddressFromMirrorNodeAsync(client, this.sender.num).get().toString();
        }
    }

    private CompletableFuture<String> getContractCallResultFromMirrorNodeAsync(Client client, String blockNumber) {
        return executeMirrorNodeRequest(client, blockNumber, false)
            .thenApply(MirrorNodeContractQuery::parseContractCallResult);
    }

    public CompletableFuture<Long> getEstimateGasFromMirrorNodeAsync(Client client) {
        return executeMirrorNodeRequest(client, "latest", true)
            .thenApply(MirrorNodeContractQuery::parseHexEstimateToLong);
    }

    private CompletableFuture<String> executeMirrorNodeRequest(Client client, String blockNumber, boolean estimate) {
        String apiEndpoint = "/contracts/call";
        String jsonPayload = createJsonPayload(this.callData, this.senderEvmAddress, this.contractEvmAddress,
            this.gasLimit, this.gasPrice, this.value, blockNumber, estimate);

        return performQueryToMirrorNodeAsync(client, apiEndpoint, jsonPayload, true)
            .exceptionally(ex -> {
                client.getLogger().error("Error while performing post request to Mirror Node: " + ex.getMessage());
                throw new CompletionException(ex);
            });
    }

    static String createJsonPayload(byte[] data, String senderAddress, String contractAddress, long gas, long gasPrice,
        long value, String blockNumber, boolean estimate) {
        String hexData = Hex.toHexString(data);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("data", hexData);
        jsonObject.addProperty("to", contractAddress);
        jsonObject.addProperty("estimate", estimate);
        jsonObject.addProperty("blockNumber", blockNumber);

        // Conditionally add fields if they are set to non-default values
        if (senderAddress != null && !senderAddress.isEmpty()) {
            jsonObject.addProperty("from", senderAddress);
        }
        if (gas > 0) {
            jsonObject.addProperty("gas", gas);
        }
        if (gasPrice > 0) {
            jsonObject.addProperty("gasPrice", gasPrice);
        }
        if (value > 0) {
            jsonObject.addProperty("value", value);
        }

        return jsonObject.toString();
    }

    static String parseContractCallResult(String responseBody) {
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        return jsonObject.get("result").getAsString();
    }

    static long parseHexEstimateToLong(String responseBody) {
        return Integer.parseInt(parseContractCallResult(responseBody).substring(2), 16);
    }
}
