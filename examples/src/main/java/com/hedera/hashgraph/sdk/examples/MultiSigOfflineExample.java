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

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * How to sign a transaction with multi-sig account.
 */
class MultiSigOfflineExample {

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
        System.out.println("Multi Sig Offline Example Start!");

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
         * Generate ED25519 private and public keys for accounts.
         */
        System.out.println("Generating ED25519 private and public keys for accounts...");

        PrivateKey user1PrivateKey = PrivateKey.generateED25519();
        System.out.println("ED25519 private key for user 1: " + user1PrivateKey);

        PublicKey user1PublicKey = user1PrivateKey.getPublicKey();
        System.out.println("ED25519 public key for user 1: " + user1PublicKey);

        PrivateKey user2PrivateKey = PrivateKey.generateED25519();
        System.out.println("ED25519 private key for user 2: " + user2PrivateKey);

        PublicKey user2PublicKey = user2PrivateKey.getPublicKey();
        System.out.println("ED25519 public key for user 2: " + user2PublicKey);

        /*
         * Step 2:
         * Create a Multi-sig account.
         */
        System.out.println("Creating new Key List..");
        KeyList keylist = new KeyList();
        keylist.add(user1PublicKey);
        keylist.add(user2PublicKey);
        System.out.println("Created Key List: " + keylist);

        System.out.println("Creating a new account...");
        TransactionResponse createAccountTransaction = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(2))
            .setKey(keylist)
            .execute(client);

        @Var
        TransactionReceipt receipt = createAccountTransaction.getReceipt(client);
        var newAccountId = receipt.accountId;

        System.out.println("Created new account with ID: " + newAccountId);

        /*
         * Step 2:
         * Create a transfer from new account to the account with ID `0.0.3`.
         */
        System.out.println("Transferring 1 Hbar from new account to the account with ID `0.0.3`...");
        TransferTransaction transferTransaction = new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
            .addHbarTransfer(Objects.requireNonNull(receipt.accountId), Hbar.from(-1))
            .addHbarTransfer(new AccountId(3), new Hbar(1))
            .freezeWith(client);

        /*
         * Step 3:
         * Convert transaction to bytes to send to signatories.
         */
        System.out.println("Converting transaction to bytes to send to signatories...");
        byte[] transactionBytes = transferTransaction.toBytes();
        Transaction<?> transactionToExecute = Transaction.fromBytes(transactionBytes);

        /*
         * Step 4:
         * Ask users to sign and return signature.
         */
        byte[] user1Signature = user1PrivateKey.signTransaction(Transaction.fromBytes(transactionBytes));
        System.out.println("User 1 signed transaction. Signature: " + Arrays.toString(user1Signature));
        byte[] user2Signature = user2PrivateKey.signTransaction(Transaction.fromBytes(transactionBytes));
        System.out.println("User 2 signed transaction. Signature: " + Arrays.toString(user2Signature));

        /*
         * Step 5:
         * Recreate the transaction from bytes.
         */
        System.out.println("Adding user's signatures to the transaction...");
        transactionToExecute.signWithOperator(client);
        transactionToExecute.addSignature(user1PrivateKey.getPublicKey(), user1Signature);
        transactionToExecute.addSignature(user2PrivateKey.getPublicKey(), user2Signature);

        /*
         * Step 6:
         * Execute recreated transaction.
         */
        System.out.println("Executing transfer transaction...");
        TransactionResponse result = transactionToExecute.execute(client);
        receipt = result.getReceipt(client);
        System.out.println("Transfer transaction was complete with status: " + receipt.status);

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
            .setAccountId(newAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(user1PrivateKey)
            .sign(user2PrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Multi Sig Offline Example Complete!");
    }
}
