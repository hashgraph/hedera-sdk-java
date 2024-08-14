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
 *  HIP-583: Expand alias support in CryptoCreate & CryptoTransfer Transactions.
 *
 *  This HIP allows any new account on Hedera to have an ECDSA derived alias, compatible with Ethereum addresses,
 *  and permits the use of this alias for all operations supported by alias today.
 *  This HIP expands support in the auto-create flow for ECDSA derived addresses to be used as the alias.
 *  It also adds support to CryptoCreate transactions for creation with an ECDSA derived alias.
 */
public class AutoCreateAccountTransferTransactionExample {

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
        System.out.println("Auto Create Account Via Transfer Transaction (HIP-583) Example Start!");

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
         * Generate an ECSDA private key.
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
         * Use the `TransferTransaction`.
         * - populate the `FromAddress` with the sender Hedera AccountID;
         * - populate the `ToAddress` with Ethereum public address.
         *
         * Note: Can transfer from public address to public address in the `TransferTransaction` for complete accounts.
         * Transfers from hollow accounts will not work because the hollow account does not have a public key
         * assigned to authorize transfers out of the account
         */
        TransferTransaction transferTransaction = new TransferTransaction()
            .addHbarTransfer(OPERATOR_ID, Hbar.from(1).negated())
            .addHbarTransfer(AccountId.fromEvmAddress(evmAddress), Hbar.from(1))
            .freezeWith(client);

        /*
         * Step 5:
         * Sign the `TransferTransaction` transaction using an existing Hedera account
         * and key paying for the transaction fee.
         */
        System.out.println("Transferring Hbar to the the new account...");
        TransactionResponse response = transferTransaction.execute(client);

        /*
         * Step 6:
         * Get the new account ID ask for the child receipts or child records for the parent transaction ID of the `TransferTransaction`
         * (the `AccountCreateTransaction` is executed as a child transaction triggered by the `TransferTransaction`).
         */
        TransactionReceipt receipt = new TransactionReceiptQuery()
            .setTransactionId(response.transactionId)
            .setIncludeChildren(true)
            .execute(client);

        AccountId newAccountId = receipt.children.get(0).accountId;
        System.out.println("The \"normal\" account ID of the given alias: " + newAccountId);

        /*
         * Step 7:
         * Get the `AccountInfo` and verify the account is a hollow account with the supplied public address (may need to verify with mirror node API).
         *
         * The Hedera Account that was created has a public address the user specified in the `TransferTransaction` `ToAddress`:
         *  - will not have a public key at this stage;
         *  - cannot do anything besides receive tokens or hbars;
         *  - the alias property of the account does not have the public address;
         *  - referred to as a hollow account.
         */
        AccountInfo accountInfo = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        if (((KeyList) accountInfo.key).isEmpty()) {
            System.out.println("The newly created account is a hollow account! (Success)");
        } else {
            throw new Exception("The newly created account is not a hollow account! (Fail)");
        }

        /*
         * Step 8:
         * Create a HAPI transaction and assign the new hollow account as the transaction fee payer.
         * Sign with the private key that corresponds to the public key on the hollow account.
         * (to enhance the hollow account to have a public key the hollow account needs to be specified as a transaction fee payer in a HAPI transaction).
         */
        System.out.println("Creating new topic...");
        TransactionReceipt receipt2 = new TopicCreateTransaction()
            .setAdminKey(publicKey)
            .setTransactionId(TransactionId.generate(newAccountId))
            .setTopicMemo("Memo")
            .freezeWith(client)
            .sign(privateKey)
            .execute(client)
            .getReceipt(client);
        System.out.println("Created new topic with ID: " + receipt2.topicId);

        /*
         * Step 9:
         * Get the `AccountInfo` for the account and return the public key on the account to show it is a complete account.
         */
        AccountInfo accountInfo2 = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("The public key of the newly created and now complete account: " + accountInfo2.key);

        /*
         * Clean up:
         * Delete created account and topic.
         */
        new AccountDeleteTransaction()
            .setTransferAccountId(OPERATOR_ID)
            .setAccountId(newAccountId)
            .freezeWith(client)
            .sign(privateKey)
            .execute(client)
            .getReceipt(client);

        new TopicDeleteTransaction()
            .setTopicId(receipt2.topicId)
            .freezeWith(client)
            .sign(privateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Auto Create Account Via Transfer Transaction (HIP-583) Example Complete!");
    }
}
