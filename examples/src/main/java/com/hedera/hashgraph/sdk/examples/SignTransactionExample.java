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
 * How to sign a transaction with a multi-sig account.
 */
class SignTransactionExample {

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

        /*
         * Step 1:
         * Generate ED25519 private and public keys pairs for a Key List.
         */
        PrivateKey user1PrivateKey = PrivateKey.generateED25519();
        PublicKey user1PublicKey = user1PrivateKey.getPublicKey();
        PrivateKey user2PrivateKey = PrivateKey.generateED25519();
        PublicKey user2PublicKey = user2PrivateKey.getPublicKey();

        /*
         * Step 2:
         * Create a Key List from keys generated in previous step.
         */
        KeyList keylist = new KeyList();
        keylist.add(user1PublicKey);
        keylist.add(user2PublicKey);

        /*
         * Step 3:
         * Create a new account with a Key List created in a previous step.
         */
        TransactionResponse createAccountTransaction = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(2))
            .setKey(keylist)
            .execute(client);

        @Var
        TransactionReceipt receipt = createAccountTransaction.getReceipt(client);
        var accountId = receipt.accountId;
        System.out.println("account id = " + accountId);

        /*
         * Step 4:
         * Create a transfer transaction and freeze it with a client.
         */
        TransferTransaction transferTransaction = new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
            .addHbarTransfer(Objects.requireNonNull(receipt.accountId), Hbar.from(-1))
            .addHbarTransfer(new AccountId(3), new Hbar(1))
            .freezeWith(client);

        /*
         * Step 5:
         * Sign the transfer transaction with all respective keys (from a Key List).
         */
        transferTransaction.signWithOperator(client);
        user1PrivateKey.signTransaction(transferTransaction);
        user2PrivateKey.signTransaction(transferTransaction);

        /*
         * Step 6:
         * Execute the transfer transaction and output its status.
         */
        TransactionResponse result = transferTransaction.execute(client);
        receipt = result.getReceipt(client);

        System.out.println(receipt.status);

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
            .setAccountId(accountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(user1PrivateKey)
            .sign(user2PrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
