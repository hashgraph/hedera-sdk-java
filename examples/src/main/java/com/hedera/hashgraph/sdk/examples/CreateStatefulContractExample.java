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

import java.util.Objects;

/**
 * How to create a stateful smart contract and call its function.
 */
class CreateStatefulContractExample {

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
        // Default max fee for all transactions executed by this client.
        client.setDefaultMaxTransactionFee(new Hbar(100));
        client.setDefaultMaxQueryPayment(new Hbar(10));

        var operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * Create a file with smart contract bytecode.
         */
        String byteCodeHex = ContractHelper.getBytecodeHex("contracts/stateful.json");
        TransactionResponse fileTransactionResponse = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file.
            .setKeys(operatorPublicKey)
            .setContents(byteCodeHex)
            .execute(client);

        TransactionReceipt fileReceipt = fileTransactionResponse.getReceipt(client);
        FileId newFileId = Objects.requireNonNull(fileReceipt.fileId);

        System.out.println("Contract bytecode file: " + newFileId);

        /*
         * Step 2:
         * Create a smart contract.
         */
        TransactionResponse contractTransactionResponse = new ContractCreateTransaction()
            // Set an admin key, so we can delete the contract later.
            .setGas(500_000)
            .setBytecodeFileId(newFileId)
            .setAdminKey(operatorPublicKey)
            .setConstructorParameters(
                new ContractFunctionParameters()
                    .addString("Hello from Hedera!"))
            .execute(client);

        TransactionReceipt contractReceipt = contractTransactionResponse.getReceipt(client);
        ContractId newContractId = Objects.requireNonNull(contractReceipt.contractId);
        System.out.println("New contract ID: " + newContractId);

        /*
         * Step 3:
         * Call smart contract function.
         */
        ContractFunctionResult contractCallResult = new ContractCallQuery()
            .setContractId(newContractId)
            .setGas(500_000)
            .setFunction("get_message")
            .setQueryPayment(new Hbar(1))
            .execute(client);

        if (contractCallResult.errorMessage != null) {
            throw new Exception("error calling contract: " + contractCallResult.errorMessage);
        }

        String message = contractCallResult.getString(0);
        System.out.println("contract returned message: " + message);

        TransactionResponse contractExecTransactionResponse = new ContractExecuteTransaction()
            .setContractId(newContractId)
            .setGas(500_000)
            .setFunction("set_message", new ContractFunctionParameters()
                .addString("hello from hedera again!"))
            .execute(client);


        // If this doesn't throw then we know the contract executed successfully.
        contractExecTransactionResponse.getReceipt(client);

        /*
         * Step 3:
         * Call smart contract function.
         */
        ContractFunctionResult contractUpdateResult = new ContractCallQuery()
            .setGas(500_000)
            .setContractId(newContractId)
            .setFunction("get_message")
            .setQueryPayment(new Hbar(1))
            .execute(client);

        if (contractUpdateResult.errorMessage != null) {
            throw new Exception("error calling contract: " + contractUpdateResult.errorMessage);
        }

        String message2 = contractUpdateResult.getString(0);
        System.out.println("contract returned message: " + message2);

        /*
         * Clean up:
         * Delete created contract.
         */
        TransactionReceipt contractDeleteResult = new ContractDeleteTransaction()
            .setContractId(newContractId)
            .setTransferAccountId(contractTransactionResponse.transactionId.accountId)
            .setMaxTransactionFee(new Hbar(1))
            .execute(client)
            .getReceipt(client);

        if (contractDeleteResult.status != Status.SUCCESS) {
            throw new Exception("error deleting contract: " + contractDeleteResult.status);
        }
        System.out.println("Contract successfully deleted");

        client.close();

        System.out.println("Example complete!");
    }
}
