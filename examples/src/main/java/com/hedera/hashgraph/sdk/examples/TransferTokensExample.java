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

import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.logger.LogLevel;
import com.hedera.hashgraph.sdk.logger.Logger;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.Objects;

/**
 * How to transfer tokens between accounts.
 */
class TransferTokensExample {

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
        System.out.println("Transfer Tokens Example Start!");

        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        PublicKey operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * Generate ED25519 key pairs.
         */
        System.out.println("Generating ED25519 key pairs for accounts...");
        PrivateKey privateKey1 = PrivateKey.generateED25519();
        PublicKey publicKey1 = privateKey1.getPublicKey();
        PrivateKey privateKey2 = PrivateKey.generateED25519();
        PublicKey publicKey2 = privateKey2.getPublicKey();

        /*
         * Step 2:
         * Create two new accounts.
         */
        System.out.println("Creating accounts...");
        Hbar initialBalance = Hbar.from(1);
        @Var TransactionResponse response = new AccountCreateTransaction()
            // The only required property here is key.
            .setKey(publicKey1)
            .setInitialBalance(initialBalance)
            .execute(client);

        // This will wait for the receipt to become available.
        @Var TransactionReceipt receipt = response.getReceipt(client);
        AccountId accountId1 = Objects.requireNonNull(receipt.accountId);
        System.out.println("Created new account with ID: " + accountId1);

        response = new AccountCreateTransaction()
            // The only required property here is key.
            .setKey(publicKey2)
            .setInitialBalance(initialBalance)
            .execute(client);

        // This will wait for the receipt to become available.
        receipt = response.getReceipt(client);
        AccountId accountId2 = Objects.requireNonNull(receipt.accountId);
        System.out.println("Created new account with ID: " + accountId2);

        /*
         * Step 3:
         * Create a Fungible Token.
         */
        System.out.println("Creating Fungible Token...");
        response = new TokenCreateTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setDecimals(3)
            .setInitialSupply(1_000_000)
            .setTreasuryAccountId(OPERATOR_ID)
            .setAdminKey(operatorPublicKey)
            .setFreezeKey(operatorPublicKey)
            .setWipeKey(operatorPublicKey)
            .setKycKey(operatorPublicKey)
            .setSupplyKey(operatorPublicKey)
            .setFreezeDefault(false)
            .execute(client);

        TokenId tokenId = Objects.requireNonNull(response.getReceipt(client).tokenId);
        System.out.println("Created Fungible Token with ID: " + tokenId);

        /*
         * Step 4:
         * Associate the token with created accounts.
         */
        System.out.println("Associating the token with created accounts...");
        new TokenAssociateTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAccountId(accountId1)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .sign(privateKey1)
            .execute(client)
            .getReceipt(client);

        System.out.println("Associated account " + accountId1 + " with token " + tokenId);

        new TokenAssociateTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAccountId(accountId2)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .sign(privateKey2)
            .execute(client)
            .getReceipt(client);

        System.out.println("Associated account " + accountId2 + " with token " + tokenId);

        /*
         * Step 5:
         * Grant token KYC for created accounts.
         */
        System.out.println("Granting token KYC for created accounts...");
        new TokenGrantKycTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAccountId(accountId1)
            .setTokenId(tokenId)
            .execute(client)
            .getReceipt(client);

        System.out.println("Granted KYC for account " + accountId1 + " on token " + tokenId);

        new TokenGrantKycTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAccountId(accountId2)
            .setTokenId(tokenId)
            .execute(client)
            .getReceipt(client);

        System.out.println("Granted KYC for account " + accountId2 + " on token " + tokenId);

        /*
         * Step 6:
         * Transfer tokens from operator's (treasury) account to the accountId1.
         */
        System.out.println("Transferring tokens from operator's (treasury) account to the `accountId1`...");
        new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .addTokenTransfer(tokenId, OPERATOR_ID, -10)
            .addTokenTransfer(tokenId, accountId1, 10)
            .execute(client)
            .getReceipt(client);

        System.out.println("Sent 10 tokens from account " + OPERATOR_ID + " to account " + accountId1 + " on token " + tokenId);

        /*
         * Step 6:
         * Transfer tokens from the accountId1 to the accountId2.
         */
        System.out.println("Transferring tokens from the `accountId1` to the `accountId2`...");
        new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .addTokenTransfer(tokenId, accountId1, -10)
            .addTokenTransfer(tokenId, accountId2, 10)
            .freezeWith(client)
            .sign(privateKey1)
            .execute(client)
            .getReceipt(client);

        System.out.println("Sent 10 tokens from account " + accountId1 + " to account " + accountId2 + " on token " + tokenId);

        /*
         * Step 6:
         * Transfer tokens from the accountId2 to the accountId1.
         */
        System.out.println("Transferring tokens from the `accountId2` to the `accountId1`...");
        new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .addTokenTransfer(tokenId, accountId2, -10)
            .addTokenTransfer(tokenId, accountId1, 10)
            .freezeWith(client)
            .sign(privateKey2)
            .execute(client)
            .getReceipt(client);

        System.out.println("Sent 10 tokens from account " + accountId2 + " to account " + accountId1 + " on token " + tokenId);

        /*
         * Clean up:
         * Delete created accounts and tokens.
         */
        new TokenWipeTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setTokenId(tokenId)
            .setAccountId(accountId1)
            .setAmount(10)
            .execute(client)
            .getReceipt(client);

        new TokenDeleteTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setTokenId(tokenId)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(accountId1)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .sign(privateKey1)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(accountId2)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .sign(privateKey2)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
