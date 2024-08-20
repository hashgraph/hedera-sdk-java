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
 * How to create a Hedera account with alias and receiver signature required.
 */
class CreateAccountWithAliasAndReceiverSignatureRequiredExample {

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
        System.out.println("Create Account With Alias And Receiver Signature Required Example Start!");

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
         * Generate ED25519 admin private key and ECSDA private key.
         */
        PrivateKey adminPrivateKey = PrivateKey.generateED25519();
        PrivateKey privateKey = PrivateKey.generateECDSA();

        /*
         * Step 2:
         * Extract the admin public key and ECDSA public key.
         */
        PublicKey adminPublicKey = adminPrivateKey.getPublicKey();
        PublicKey publicKey = privateKey.getPublicKey();

        /*
         * Step 3:
         * Extract Ethereum public address.
         */
        EvmAddress evmAddress = publicKey.toEvmAddress();
        System.out.println("EVM address of the new account: " + evmAddress);

        /*
         * Step 4:
         * Create new account.
         *
         * Populate setAlias(evmAddress) field with the Ethereum public address
         * and the setReceiverSignatureRequired to true.
         */
        System.out.println("Creating new account...(with alias and receiver signature required).");
        AccountCreateTransaction accountCreateTx = new AccountCreateTransaction()
            .setReceiverSignatureRequired(true)
            .setInitialBalance(Hbar.from(1))
            .setKey(adminPublicKey)
            .setAlias(evmAddress)
            .freezeWith(client);

        /*
         * Step 5:
         * Sign the AccountCreateTransaction transaction with both the new private key and the admin key.
         */
        accountCreateTx.sign(adminPrivateKey).sign(privateKey);
        AccountId newAccountId = accountCreateTx.execute(client).getReceipt(client).accountId;
        Objects.requireNonNull(newAccountId);
        System.out.println("Created account with ID: " + newAccountId);

         /*
         * Step 6:
         * Get the AccountInfo and show that the account has contractAccountId.
         */
        AccountInfo newAccountInfo = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        if (newAccountInfo.contractAccountId != null) {
            System.out.println("The newly account has alias: " + newAccountInfo.contractAccountId);
        } else {
            throw new Exception("The newly account doesn't have alias! (Fail)");
        }

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
            .setAccountId(newAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(adminPrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Create Account With Alias And Receiver Signature Required Example Complete!");
    }
}
