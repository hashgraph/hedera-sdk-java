/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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
 * HIP-745: Optionally send transaction data without required transaction fields.
 * How to serialize incomplete transaction, deserialize it, complete and execute.
 */
class TransactionSerializationExample {

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
         * Check Hbar balance of the sender and recipient.
         */
        AccountId recipientId = AccountId.fromString("0.0.3");
        Hbar senderBalanceBefore = new AccountBalanceQuery()
            .setAccountId(OPERATOR_ID)
            .execute(client)
            .hbars;

        Hbar recipientBalanceBefore = new AccountBalanceQuery()
            .setAccountId(recipientId)
            .execute(client)
            .hbars;

        System.out.println("" + OPERATOR_ID + " balance = " + senderBalanceBefore);
        System.out.println("" + recipientId + " balance = " + recipientBalanceBefore);

        /*
         * Step 2:
         * Create the transfer transaction with adding only Hbar transfer which credits the operator.
         */
        Hbar transferAmount = Hbar.fromTinybars(10_000);
        var transferTransaction = new TransferTransaction()
            // `.addSender` and `.addRecipient` can be called as many times as you want as long as the total sum from
            // both sides is equivalent.
            .addHbarTransfer(OPERATOR_ID, transferAmount.negated());

        /*
         * Step 3:
         * Serialize the transfer transaction.
         */
        var transactionBytes = transferTransaction.toBytes();

        /*
         * Step 4:
         * Deserialize the transfer transaction.
         */
        TransferTransaction transferTransactionDeserialized = (TransferTransaction) Transaction.fromBytes(transactionBytes);

        /*
         * Step 5:
         * Complete the transfer transaction-- add Hbar transfer which debits Hbar to the recipient.
         * And execute the transfer transaction.
         */
        var transactionResponse = transferTransactionDeserialized
            .addHbarTransfer(recipientId, transferAmount)
            .setTransactionMemo("transfer test")
            .execute(client);

        System.out.println("transaction ID: " + transactionResponse);
        TransactionRecord record = transactionResponse.getRecord(client);
        System.out.println("transferred " + transferAmount + "...");

        /*
         * Step 6:
         * Check Hbar balance of the sender and recipient after transfer transaction was executed.
         */
        Hbar senderBalanceAfter = new AccountBalanceQuery()
            .setAccountId(OPERATOR_ID)
            .execute(client)
            .hbars;

        Hbar receiptBalanceAfter = new AccountBalanceQuery()
            .setAccountId(recipientId)
            .execute(client)
            .hbars;

        System.out.println("" + OPERATOR_ID + " balance = " + senderBalanceAfter);
        System.out.println("" + recipientId + " balance = " + receiptBalanceAfter);
        System.out.println("Transfer memo: " + record.transactionMemo);

        /*
         * Clean up:
         */
        client.close();
        System.out.println("Example complete!");
    }
}
