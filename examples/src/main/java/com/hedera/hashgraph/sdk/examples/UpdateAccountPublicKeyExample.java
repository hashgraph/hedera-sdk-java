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
 * How to update account's key.
 */
class UpdateAccountPublicKeyExample {

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
        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        client.setDefaultMaxTransactionFee(new Hbar(10));

        /*
         * Step 1:
         * Generate ED25519 private and public keys for accounts.
         */
        PrivateKey privateKey1 = PrivateKey.generateED25519();
        PublicKey publicKey1 = privateKey1.getPublicKey();
        PrivateKey privateKey2 = PrivateKey.generateED25519();
        PublicKey publicKey2 = privateKey2.getPublicKey();

        /*
         * Step 2:
         * Create a new account.
         */
        TransactionResponse acctTransactionResponse = new AccountCreateTransaction()
            .setKey(publicKey1)
            .setInitialBalance(new Hbar(1))
            .execute(client);

        System.out.println("transaction ID: " + acctTransactionResponse);
        AccountId accountId = Objects.requireNonNull(acctTransactionResponse.getReceipt(client).accountId);
        System.out.println("account = " + accountId);
        System.out.println("key = " + privateKey1.getPublicKey());

        /*
         * Step 2:
         * Update account's key.
         */
        System.out.println(" :: update public key of account " + accountId);
        System.out.println("set key = " + privateKey2.getPublicKey());

        TransactionResponse accountUpdateTransactionResponse = new AccountUpdateTransaction()
            .setAccountId(accountId)
            .setKey(publicKey2)
            .freezeWith(client)
            // Sign with the previous key and the new key.
            .sign(privateKey1)
            .sign(privateKey2)
            // Execute will implicitly sign with the operator.
            .execute(client);

        System.out.println("transaction ID: " + accountUpdateTransactionResponse);

        // (Important!) Wait for the transaction to complete by querying the receipt.
        accountUpdateTransactionResponse.getReceipt(client);

        /*
         * Step 3:
         * Get account info to confirm the key was changed.
         */
        System.out.println(" :: getAccount and check our current key");

        AccountInfo info = new AccountInfoQuery()
            .setAccountId(accountId)
            .execute(client);

        System.out.println("key = " + info.key);

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
            .setAccountId(accountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(privateKey2)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
