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
package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.logger.LogLevel;
import com.hedera.hashgraph.sdk.logger.Logger;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * How to create a simple stateless smart contract and call its function.
 */
class CreateSimpleContractExample {

    // See `.env.sample` in the `examples` folder root for how to specify values below
    // or set environment variables with the same names.

    // Operator's account ID.
    // Used to sign and pay for operations on Hedera.
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    // Operator's private key.
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // `HEDERA_NETWORK` defaults to `testnet` if not specified in dotenv file
    // Networks can be: `localhost`, `testnet`, `previewnet`, `mainnet`.
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    // `SDK_LOG_LEVEL` defaults to `SILENT` if not specified in dotenv file
    // Log levels can be: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `SILENT`.
    // Important pre-requisite: set simple logger log level to same level as the SDK_LOG_LEVEL,
    // for example via VM options: `-Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace`
    private static final String SDK_LOG_LEVEL = Dotenv.load().get("SDK_LOG_LEVEL", "SILENT");

    public static void main(String[] args) throws Exception {
        System.out.println("Create Simple Contract Example Start!");

        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        var operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * Create a file with smart contract bytecode.
         */
        System.out.println("Creating new bytecode file...");
        String byteCodeHex = ContractHelper.getBytecodeHex("contracts/hello_world.json");

        TransactionResponse fileTransactionResponse = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file.
            .setKeys(operatorPublicKey)
            .setContents(byteCodeHex.getBytes(StandardCharsets.UTF_8))
            .setMaxTransactionFee(new Hbar(2))
            .execute(client);

        TransactionReceipt fileReceipt = fileTransactionResponse.getReceipt(client);
        FileId newFileId = Objects.requireNonNull(fileReceipt.fileId);

        System.out.println("Created new bytecode file with ID: " + newFileId);

        /*
         * Step 2:
         * Create a smart contract.
         */
        System.out.println("Creating new contract...");
        TransactionResponse contractTransactionResponse = new ContractCreateTransaction()
            .setGas(500_000)
            .setBytecodeFileId(newFileId)
            // Set an admin key, so we can delete the contract later.
            .setAdminKey(operatorPublicKey)
            .setMaxTransactionFee(new Hbar(16))
            .execute(client);

        TransactionReceipt contractReceipt = contractTransactionResponse.getReceipt(client);
        ContractId newContractId = Objects.requireNonNull(contractReceipt.contractId);
        System.out.println("Created new contract with ID: " + newContractId);

        /*
         * Step 3:
         * Call smart contract function.
         */
        System.out.println("Calling contract function \"greet\"...");
        ContractFunctionResult contractCallResult = new ContractCallQuery()
            .setGas(500_000)
            .setContractId(newContractId)
            .setFunction("greet")
            .setQueryPayment(new Hbar(1))
            .execute(client);

        if (contractCallResult.errorMessage != null) {
            throw new Exception("Error calling contract function \"greet\": " + contractCallResult.errorMessage);
        }

        String message = contractCallResult.getString(0);
        System.out.println("Contract call result (\"greet\" function returned): " + message);

        /*
         * Clean up:
         * Delete created contract.
         */
        new ContractDeleteTransaction()
            .setContractId(newContractId)
            .setTransferAccountId(contractTransactionResponse.transactionId.accountId)
            .setMaxTransactionFee(new Hbar(1))
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Create Simple Contract Example Complete!");
    }
}
