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
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * How to create a simple stateless smart contract and call its function.
 */
class CreateSimpleContractExample {

    // See `.env.sample` in the `examples` folder root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public static void main(String[] args) throws Exception {
        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        var operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * Create a file with smart contract bytecode.
         */
        String byteCodeHex = ContractHelper.getBytecodeHex("contracts/hello_world.json");

        TransactionResponse fileTransactionResponse = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file.
            .setKeys(operatorPublicKey)
            .setContents(byteCodeHex.getBytes(StandardCharsets.UTF_8))
            .setMaxTransactionFee(new Hbar(2))
            .execute(client);

        TransactionReceipt fileReceipt = fileTransactionResponse.getReceipt(client);
        FileId newFileId = Objects.requireNonNull(fileReceipt.fileId);

        System.out.println("Contract bytecode file: " + newFileId);

        /*
         * Step 2:
         * Create a smart contract.
         */
        TransactionResponse contractTransactionResponse = new ContractCreateTransaction()
            .setGas(500_000)
            .setBytecodeFileId(newFileId)
            // Set an admin key, so we can delete the contract later.
            .setAdminKey(operatorPublicKey)
            .setMaxTransactionFee(new Hbar(16))
            .execute(client);

        TransactionReceipt contractReceipt = contractTransactionResponse.getReceipt(client);
        System.out.println(contractReceipt);

        ContractId newContractId = Objects.requireNonNull(contractReceipt.contractId);
        System.out.println("New contract ID: " + newContractId);

        /*
         * Step 3:
         * Call smart contract function.
         */
        ContractFunctionResult contractCallResult = new ContractCallQuery()
            .setGas(500_000)
            .setContractId(newContractId)
            .setFunction("greet")
            .setQueryPayment(new Hbar(1))
            .execute(client);

        if (contractCallResult.errorMessage != null) {
            throw new Exception("error calling contract: " + contractCallResult.errorMessage);
        }

        String message = contractCallResult.getString(0);
        System.out.println("contract message: " + message);

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

        System.out.println("Example complete!");
    }
}
