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

import java.util.Collections;
import java.util.Objects;

/**
 * How to create a Hedera account with threshold key.
 */
class CreateAccountThresholdKeyExample {

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

        /*
         * Step 1:
         * Generate three new Ed25519 private, public key pairs.
         * You do not need the private keys to create the Threshold Key List,
         * you only need the public keys, and if you're doing things correctly,
         * you probably shouldn't have these private keys.
         */
        PrivateKey[] privateKeys = new PrivateKey[3];
        PublicKey[] publicKeys = new PublicKey[3];
        for (int i = 0; i < 3; i++) {
            PrivateKey key = PrivateKey.generateED25519();
            privateKeys[i] = key;
            publicKeys[i] = key.getPublicKey();
        }

        System.out.println("public keys: ");
        for (Key key : publicKeys) {
            System.out.println(key);
        }

        /*
         * Step 2:
         * Create a Key List. Require 2 of the 3 keys we generated to sign on anything modifying this account.
         */
        KeyList transactionKey = KeyList.withThreshold(2);
        Collections.addAll(transactionKey, publicKeys);

        /*
         * Step 2:
         * Create a new account setting a Key List from a previous step as an account's key.
         */
        TransactionResponse transactionResponse = new AccountCreateTransaction()
            .setKey(transactionKey)
            .setInitialBalance(new Hbar(1))
            .execute(client);

        TransactionReceipt receipt = transactionResponse.getReceipt(client);
        AccountId newAccountId = Objects.requireNonNull(receipt.accountId);
        System.out.println("account = " + newAccountId);

        /*
         * Step 2:
         * Create a transfer transaction from a newly created account to demonstrate the signing process (threshold).
         */
        TransactionResponse transferTransactionResponse = new TransferTransaction()
            .addHbarTransfer(newAccountId, new Hbar(1).negated())
            .addHbarTransfer(new AccountId(3), new Hbar(1))
            // To manually sign, you must explicitly build the Transaction
            .freezeWith(client)
            // we sign with 2 of the 3 keys
            .sign(privateKeys[0])
            .sign(privateKeys[1])
            .execute(client);

        // (Important!) Wait for the transfer to go to consensus.
        transferTransactionResponse.getReceipt(client);

        Hbar accountBalanceAfterTransfer = new AccountBalanceQuery()
            .setAccountId(newAccountId)
            .execute(client)
            .hbars;

        System.out.println("Account balance after transfer: " + accountBalanceAfterTransfer);

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
            .setTransferAccountId(OPERATOR_ID)
            .setAccountId(newAccountId)
            .freezeWith(client)
            .sign(privateKeys[0])
            .sign(privateKeys[1])
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
