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
 * How to stake to some account.
 *
 */
class StakingExample {

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
        System.out.println("Staking Example Start!");

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
        System.out.println("Generating ED25519 key pair...");
        PrivateKey privateKey = PrivateKey.generateED25519();
        PublicKey publicKey = privateKey.getPublicKey();

        /*
         * Step 2:
         * Create an account and stake to an account ID.
         *
         * In this case we're staking to account ID 3 which happens to be
         * the account ID of node 0, we're only doing this as an example.
         * If you really want to stake to node 0, you should use setStakedNodeId() instead.
         */
        System.out.println("Creating new account with staked account ID...");
        AccountId stakedAccountId = AccountId.fromString("0.0.3");
        AccountId newAccountId = new AccountCreateTransaction()
            .setKey(publicKey)
            .setInitialBalance(Hbar.from(1))
            .setStakedAccountId(stakedAccountId)
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(newAccountId);
        System.out.println("Created new account with ID: " + newAccountId);

        // Show the required key used to sign the account update transaction to
        // stake the accounts Hbar i.e. the fee payer key and key to authorize
        // changes to the account should be different.
        System.out.println("Key required to update staking information: " + publicKey);
        System.out.println("Fee payer or operator key: " + client.getOperatorPublicKey());

        /*
         * Step 3:
         * Query the account info, it should show the staked account ID to be 0.0.3.
         */
        AccountInfo info = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        if (info.stakingInfo.stakedAccountId.equals(stakedAccountId)) {
            System.out.println("New account staking info: " + info.stakingInfo);
        } else {
            throw new Exception("Staked account ID was not set correctly! (Fail)");
        }

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
            .setAccountId(newAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(privateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Staking Example Complete!");
    }
}
