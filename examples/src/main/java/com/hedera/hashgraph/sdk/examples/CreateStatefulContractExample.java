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

import java.util.Objects;

/**
 * How to create a stateful smart contract and call its function.
 */
class CreateStatefulContractExample {

    /*
     * See .env.sample in the examples folder root for how to specify values below
     * or set environment variables with the same names.
     */

    /**
     * Operator's account ID.
     * Used to sign and pay for operations on Hedera.
     */
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    /**
     * Operator's private key.
     */
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    /**
     * HEDERA_NETWORK defaults to testnet if not specified in dotenv file.
     * Network can be: localhost, testnet, previewnet or mainnet.
     */
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    /**
     * SDK_LOG_LEVEL defaults to SILENT if not specified in dotenv file.
     * Log levels can be: TRACE, DEBUG, INFO, WARN, ERROR, SILENT.
     * <p>
     * Important pre-requisite: set simple logger log level to same level as the SDK_LOG_LEVEL,
     * for example via VM options: -Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace
     */
    private static final String SDK_LOG_LEVEL = Dotenv.load().get("SDK_LOG_LEVEL", "SILENT");

    public static void main(String[] args) throws Exception {
        System.out.println("Create Stateful Contract Example Start!");

        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));
        // Default max fee for all transactions executed by this client.
        client.setDefaultMaxTransactionFee(Hbar.from(100));
        client.setDefaultMaxQueryPayment(Hbar.from(10));

        var operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * Create a file with smart contract bytecode.
         */
        System.out.println("Creating new bytecode file...");
        String byteCodeHex = ContractHelper.getBytecodeHex("contracts/stateful.json");

        TransactionResponse fileTransactionResponse = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file.
            .setKeys(operatorPublicKey)
            .setContents(byteCodeHex)
            .execute(client);

        TransactionReceipt fileReceipt = fileTransactionResponse.getReceipt(client);
        FileId newFileId = Objects.requireNonNull(fileReceipt.fileId);

        System.out.println("Created new bytecode file with ID: " + newFileId);

        /*
         * Step 2:
         * Create a smart contract.
         */
        TransactionResponse contractTransactionResponse = new ContractCreateTransaction()
            // Set an Admin Key, so we can delete the contract later.
            .setGas(150_000)
            .setBytecodeFileId(newFileId)
            .setAdminKey(operatorPublicKey)
            .setConstructorParameters(
                new ContractFunctionParameters()
                    .addString("Hello from Hedera!"))
            .execute(client);

        TransactionReceipt contractReceipt = contractTransactionResponse.getReceipt(client);
        ContractId newContractId = Objects.requireNonNull(contractReceipt.contractId);
        System.out.println("Created new contract with ID: " + newContractId);

        /*
         * Step 3:
         * Call smart contract function.
         */
        System.out.println("Calling contract function \"get_message\"...");
        ContractFunctionResult contractCallResult = new ContractCallQuery()
            .setContractId(newContractId)
            .setGas(100_000)
            .setFunction("get_message")
            .setMaxQueryPayment(Hbar.from(1))
            .execute(client);

        if (contractCallResult.errorMessage != null) {
            throw new Exception("Error calling contract function \"get_message\": " + contractCallResult.errorMessage);
        }

        String message = contractCallResult.getString(0);
        System.out.println("Contract call result (\"get_message\" function returned): " + message);

        System.out.println("Calling contract function \"set_message\"...");
        TransactionResponse contractExecTransactionResponse = new ContractExecuteTransaction()
            .setContractId(newContractId)
            .setGas(100_000)
            .setFunction("set_message", new ContractFunctionParameters()
                .addString("hello from hedera again!"))
            .execute(client);

        // If this doesn't throw then we know the contract executed successfully.
        contractExecTransactionResponse.getReceipt(client);

        /*
         * Step 3:
         * Call smart contract function.
         */
        System.out.println("Calling contract function \"get_message\"...");
        ContractFunctionResult contractUpdateResult = new ContractCallQuery()
            .setGas(100_000)
            .setContractId(newContractId)
            .setFunction("get_message")
            .setMaxQueryPayment(Hbar.from(1))
            .execute(client);

        if (contractUpdateResult.errorMessage != null) {
            throw new Exception("Error calling contract function \"get_message\": " + contractUpdateResult.errorMessage);
        }

        String message2 = contractUpdateResult.getString(0);
        System.out.println("Contract call result (\"get_message\" function returned): " + message2);

        /*
         * Clean up:
         * Delete created contract.
         */
        new ContractDeleteTransaction()
            .setContractId(newContractId)
            .setTransferAccountId(contractTransactionResponse.transactionId.accountId)
            .setMaxTransactionFee(Hbar.from(1))
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Create Stateful Contract Example Complete!");
    }
}
