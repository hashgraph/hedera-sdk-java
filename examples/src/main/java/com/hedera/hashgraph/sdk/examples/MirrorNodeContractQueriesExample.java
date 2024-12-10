/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2023 - 2024 Hedera Hashgraph, LLC
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

package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractCallQuery;
import com.hedera.hashgraph.sdk.ContractCreateTransaction;
import com.hedera.hashgraph.sdk.ContractFunctionParameters;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.MirrorNodeContractCallQuery;
import com.hedera.hashgraph.sdk.MirrorNodeContractEstimateGasQuery;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.logger.LogLevel;
import com.hedera.hashgraph.sdk.logger.Logger;
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.bouncycastle.util.encoders.Hex;

public class MirrorNodeContractQueriesExample {
    /*
     * See .env.sample in the examples folder root for how to specify values below
     * or set environment variables with the same names.
     */

    /**
     * Operator's account ID. Used to sign and pay for operations on Hedera.
     */
    private static final AccountId OPERATOR_ID = AccountId.fromString(
        Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    /**
     * Operator's private key.
     */
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(
        Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    /**
     * HEDERA_NETWORK defaults to testnet if not specified in dotenv file. Network can be: localhost, testnet,
     * previewnet or mainnet.
     */
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    /**
     * SDK_LOG_LEVEL defaults to SILENT if not specified in dotenv file. Log levels can be: TRACE, DEBUG, INFO, WARN,
     * ERROR, SILENT.
     * <p>
     * Important pre-requisite: set simple logger log level to same level as the SDK_LOG_LEVEL, for example via VM
     * options: -Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace
     */
    private static final String SDK_LOG_LEVEL = Dotenv.load().get("SDK_LOG_LEVEL", "SILENT");

    private static final String SMART_CONTRACT_BYTECODE = "6080604052348015600e575f80fd5b5061014e8061001c5f395ff3fe608060405234801561000f575f80fd5b5060043610610029575f3560e01c8063ce6d41de1461002d575b5f80fd5b61003561004b565b60405161004291906100f8565b60405180910390f35b60606040518060400160405280600581526020017f68656c6c6f000000000000000000000000000000000000000000000000000000815250905090565b5f81519050919050565b5f82825260208201905092915050565b8281835e5f83830152505050565b5f601f19601f8301169050919050565b5f6100ca82610088565b6100d48185610092565b93506100e48185602086016100a2565b6100ed816100b0565b840191505092915050565b5f6020820190508181035f83015261011081846100c0565b90509291505056fea264697066735822122073f43039f9146f50acc1b1f6e211c2588bf825fa9fef2178482dd5c63009edcc64736f6c634300081a0033";

    public static void main(String[] args) throws Exception {
        System.out.println("Mirror Node contract queries Example Start!");

        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        /*
         * Step 1:
         * Create the contract
         */
        var response = new ContractCreateTransaction()
            .setGas(200_000)
            .setBytecode(Hex.decode(SMART_CONTRACT_BYTECODE))
            .setContractMemo("Simple contract with string field")
            .execute(client);

        var contractId = Objects.requireNonNull(response.getReceipt(client).contractId);
        System.out.println("Created new contract with ID: " + contractId);

        /*
         * Step 3:
         * Wait for mirror node to import data
         */
        Thread.sleep(4000);

        /*
         * Step 4:
         * Estimate the gas needed
         */
        var gas = new MirrorNodeContractEstimateGasQuery()
            .setContractId(contractId)
            .setSender(client.getOperatorAccountId())
            .setGasLimit(30_000)
            .setGasPrice(1234)
            .setFunction("getMessage")
            .execute(client);

        System.out.println("Gas needed for this query: " + gas);

        /*
         * Step 5:
         * Do the query against the consensus node using the estimated gas
         */
        var callQuery = new ContractCallQuery()
            .setContractId(contractId)
            .setGas(gas)
            .setFunction("getMessage")
            .setQueryPayment(new Hbar(1));

        var result = callQuery
            .execute(client);

        /*
         * Step 6:
         * Simulate the transaction for free, using the mirror node
         */
        var simulationResult = new MirrorNodeContractCallQuery()
            .setContractId(contractId)
            .setSender(client.getOperatorAccountId())
            .setGasLimit(30_000)
            .setBlockNumber(10000)
            .setGasPrice(1234)
            .setFunction("getMessage")
            .execute(client);

        // Decode the result since it's coming in ABI Hex format from the Mirror Node
        var decodedResult = decodeABIHexString(simulationResult);
        System.out.println("Simulation result: " + decodedResult);
        System.out.println("Contract call result: " + result.getString(0));
    }

    /**
     * Decodes a hex-encoded ABI (Application Binary Interface) string into a UTF-8 string.
     * <p>
     * The function assumes the input follows the ABI encoding standard for dynamic data. Specifically, it parses the
     * length of the dynamic data and extracts the corresponding substring.
     * <p>
     * The structure of the input hex string is as follows: - The first 64 characters represent metadata, such as
     * offsets and other header information. - Characters from index 64 to 128 encode the length of the dynamic data in
     * bytes. - Characters from index 128 onward represent the actual dynamic data.
     * <p>
     * This method removes the `0x` prefix if present, parses the length, and decodes the dynamic data into UTF-8.
     *
     * @param hex the hex string to decode, which follows the ABI encoding standard
     * @return the decoded UTF-8 string
     */
    private static String decodeABIHexString(String hex) {
        // Trim 0x at the beginning
        if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }

        // Extract the length of the data by parsing the substring from position 64 to 128 as a hexadecimal integer
        // This section represents the length of the dynamic data, specifically the number of bytes in the string or array
        int length = Integer.parseInt(hex.substring(64, 128), 16);

        // Using the extracted length, the code calculates the substring containing the actual data starting from position 128.
        String hexStringData = hex.substring(128, 128 + length * 2);

        byte[] bytes = new byte[length];
        // Iterate through the extracted hex data, two characters at a time, converting each pair to a byte and storing it in a byte array.
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) Integer.parseInt(hexStringData.substring(i * 2, i * 2 + 2), 16);
        }

        // Convert to UTF 8
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
