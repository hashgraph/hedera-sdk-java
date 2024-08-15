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

import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.logger.LogLevel;
import com.hedera.hashgraph.sdk.logger.Logger;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

/**
 * How to transfer Hbar or tokens to a Hedera account using their public-address (HIP-583).
 */
class TransferUsingEvmAddressExample {

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
        System.out.println("Transfer Using Evm Address Example Start!");

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
         * Create an ECSDA private key.
         */
        PrivateKey privateKey = PrivateKey.generateECDSA();

        /*
         * Step 2:
         * Extract the ECDSA public key.
         */
        PublicKey publicKey = privateKey.getPublicKey();

        /*
         * Step 3:
         * Extract the Ethereum public address.
         */
        EvmAddress evmAddress = publicKey.toEvmAddress();
        System.out.println("EVM address of the new account: " + evmAddress);

        /*
         * Step 4:
         * Transfer tokens using the TransferTransaction to the Ethereum Account Address.
         * - the from field should be a complete account that has a public address;
         * - the to field should be to a public address (to create a new account).
         */
        System.out.println("Transferring Hbar to the the new account...");
        TransferTransaction transferTx = new TransferTransaction()
            .addHbarTransfer(OPERATOR_ID, Hbar.from(10).negated())
            .addHbarTransfer(evmAddress, Hbar.from(10))
            .freezeWith(client);

        TransferTransaction transferTxSign = transferTx.sign(OPERATOR_KEY);
        TransactionResponse transferTxSubmit = transferTxSign.execute(client);

        /*
         * Step 5:
         * Get the child receipt or child record to return the Hedera Account ID for the new account that was created.
         */
        TransactionReceipt receipt = new TransactionReceiptQuery()
            .setTransactionId(transferTxSubmit.transactionId)
            .setIncludeChildren(true)
            .execute(client);

        AccountId newAccountId = receipt.children.get(0).accountId;
        System.out.println("The \"normal\" account ID of the given alias: " + newAccountId);

        /*
         * Step 6:
         * Get the AccountInfo on the new account and show it is a hollow account by not having a public key.
         */
        AccountInfo accountInfo = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("New account info: " + accountInfo);

        /*
         * Step 7:
         * Use the hollow account as a transaction fee payer in a HAPI transaction.
         */
        System.out.println("Setting new account as client's operator...");
        client.setOperator(newAccountId, privateKey);
        PrivateKey newPrivateKey = PrivateKey.generateED25519();
        PublicKey newPublicKey = newPrivateKey.getPublicKey();

        System.out.println("Creating new account...");
        AccountCreateTransaction transaction = new AccountCreateTransaction()
            .setKey(newPublicKey)
            .freezeWith(client);

        /*
         * Step 8:
         * Sign the transaction with ECDSA private key.
         */
        AccountCreateTransaction transactionSign = transaction.sign(privateKey);
        TransactionResponse transactionSubmit = transactionSign.execute(client);
        TransactionReceipt status = transactionSubmit.getReceipt(client);
        var accountId = status.accountId;
        System.out.println("Created new account with ID: " + accountId);

        /*
         * Step 9:
         * Get the AccountInfo of the account and show the account is now a complete account
         * by returning the public key on the account.
         */
        AccountInfo accountInfo2 = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("The public key of the newly created (and now complete) account: " + accountInfo2.key);

        /*
         * Clean up:
         * Delete created accounts.
         */
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        new AccountDeleteTransaction()
            .setAccountId(newAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(privateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(accountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(newPrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Transfer Using Evm Address Example Complete!");
    }
}
