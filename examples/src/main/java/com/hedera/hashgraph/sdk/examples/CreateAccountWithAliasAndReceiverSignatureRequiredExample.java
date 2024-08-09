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
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

/**
 * How to create a Hedera account with alias and receiver signature required.
 */
class CreateAccountWithAliasAndReceiverSignatureRequiredExample {

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
         * Generate an ED25519 admin private key and ECSDA private key.
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
         * Extract the Ethereum public address.
         */
        EvmAddress evmAddress = publicKey.toEvmAddress();
        System.out.println(evmAddress);

        /*
         * Step 4:
         * Use the `AccountCreateTransaction` and populate `setAlias(evmAddress)` field with the Ethereum public address
         * and the `setReceiverSignatureRequired` to `true`.
         */
        AccountCreateTransaction accountCreateTransaction = new AccountCreateTransaction()
            .setReceiverSignatureRequired(true)
            .setInitialBalance(new Hbar(1))
            .setKey(adminPublicKey)
            .setAlias(evmAddress)
            .freezeWith(client);

        /*
         * Step 5:
         * Sign the `AccountCreateTransaction` transaction with both the new private key and the admin key.
         */
        accountCreateTransaction.sign(adminPrivateKey).sign(privateKey);
        AccountId newAccountId = accountCreateTransaction.execute(client).getReceipt(client).accountId;

        System.out.println("New account ID: " + newAccountId);

         /*
         * Step 6:
         * Get the `AccountInfo` and show that the account has `contractAccountId`.
         */
        AccountInfo accountInfo = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        if (accountInfo.contractAccountId != null) {
            System.out.println("The new account has alias " + accountInfo.contractAccountId);
        } else {
            throw new Exception("The new account doesn't have alias");
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

        System.out.println("Example complete!");
    }
}
