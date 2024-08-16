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
 * How to delete a Hedera account.
 *
 * TODO: delete this example as it is a duplicate of `CreateAccountExample`?
 */
class DeleteAccountExample {

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
        System.out.println("Delete Account Example Start!");

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
         * Generate ED25519 key pair.
         */
        PrivateKey newPrivateKey = PrivateKey.generateED25519();
        PublicKey newPublicKey = newPrivateKey.getPublicKey();
        System.out.println("Future account private key: " + newPrivateKey);
        System.out.println("Future account public key: " + newPublicKey);

        /*
         * Step 2:
         * Create new account.
         */
        System.out.println("Creating new account...");
        TransactionResponse transactionResponse = new AccountCreateTransaction()
            // The only required property here is `key`.
            .setKey(newPublicKey)
            .setInitialBalance(Hbar.from(1))
            .execute(client);

        // This will wait for the receipt to become available.
        TransactionReceipt receipt = transactionResponse.getReceipt(client);
        AccountId newAccountId = Objects.requireNonNull(receipt.accountId);
        System.out.println("Created new account with ID: " + newAccountId);

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
            // Note: the transaction ID has to use the ID of the account being deleted.
            .setTransactionId(TransactionId.generate(newAccountId))
            .setAccountId(newAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(newPrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Delete Account Example Complete!");
    }
}
