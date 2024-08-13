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
 * How to schedule identical transactions.
 */
class ScheduleIdenticalTransactionExample {

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
         * Create key pairs, clients and accounts.
         */
        System.out.println("Threshold key example.");
        System.out.println("Keys:");
        PrivateKey[] privateKeys = new PrivateKey[3];
        PublicKey[] publicKeys = new PublicKey[3];
        Client[] clients = new Client[3];
        AccountId[] accounts = new AccountId[3];

        @Var
        ScheduleId scheduleID = null;

        for (int i = 0; i < 3 ; i++) {
            PrivateKey newPrivateKey = PrivateKey.generateED25519();
            PublicKey newPublicKey = newPrivateKey.getPublicKey();

            privateKeys[i] = newPrivateKey;
            publicKeys[i] = newPublicKey;

            System.out.println("Key #" + i + ":");
            System.out.println("private = " + privateKeys[i]);
            System.out.println("public = " + publicKeys[i]);

            TransactionResponse createResponse = new AccountCreateTransaction()
                .setKey(newPublicKey)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            // Make sure the transaction succeeded.
            TransactionReceipt transactionReceipt = createResponse.getReceipt(client);

            Client newClient = ClientHelper.forName(HEDERA_NETWORK);
            newClient.setOperator(Objects.requireNonNull(transactionReceipt.accountId), newPrivateKey);
            clients[i] = newClient;
            accounts[i] = transactionReceipt.accountId;

            System.out.println("account = " + accounts[i]);
        }

        /*
         * Step 2:
         * Create a threshold key with a threshold of 2 and length of 3 requires
         * (at least 2 of the 3 keys to sign anything modifying the account).
         */
        KeyList keyList = KeyList.withThreshold(2);
        Collections.addAll(keyList, publicKeys);

        /*
         * Step 3:
         * Create a new account with the Key List from previous step.
         */
        // We are using all of these keys, so the scheduled transaction doesn't automatically go through.
        // It works perfectly fine with just one key.
        TransactionResponse createResponse = new AccountCreateTransaction()
            // The key that must sign each transfer out of the account. If receiverSigRequired is true, then
            // it must also sign any transfer into the account.
            .setKey(keyList)
            .setInitialBalance(new Hbar(10))
            .execute(client);

        // Make sure the transaction succeeded.
        TransactionReceipt receipt = createResponse.getReceipt(client);

        AccountId thresholdAccount = receipt.accountId;
        System.out.println("threshold account = " + thresholdAccount);

        /*
         * Step 4:
         * Each `loopClient` creates an identical transaction, sending 1 Hbar to each of the created accounts,
         * sent from the threshold Account.
         */
        for (Client loopClient : clients) {
            AccountId operatorId = loopClient.getOperatorAccountId();

            TransferTransaction tx = new TransferTransaction();
            for (AccountId account : accounts) {
                tx.addHbarTransfer(account, new Hbar(1));
            }
            tx.addHbarTransfer(Objects.requireNonNull(thresholdAccount), new Hbar(3).negated());

            ScheduleCreateTransaction scheduledTx = new ScheduleCreateTransaction()
                .setScheduledTransaction(tx);

            scheduledTx.setPayerAccountId(thresholdAccount);

            TransactionResponse response = scheduledTx.execute(loopClient);

            TransactionReceipt loopReceipt = new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(loopClient);

            System.out.println("operator [" + operatorId + "]: scheduleID = " + loopReceipt.scheduleId);

            // Save the schedule ID, so that it can be asserted for each loopClient submission.
            if (scheduleID == null) {
                scheduleID = loopReceipt.scheduleId;
            }

            if (!scheduleID.equals(Objects.requireNonNull(loopReceipt.scheduleId))) {
                throw new Exception("invalid generated schedule id, expected " + scheduleID + ", got " + loopReceipt.scheduleId);
            }

            // If the status return by the receipt is related to already created, execute a schedule sign transaction.
            if (loopReceipt.status == Status.IDENTICAL_SCHEDULE_ALREADY_CREATED) {
                TransactionResponse signTransaction = new ScheduleSignTransaction()
                    .setScheduleId(scheduleID)
                    .setNodeAccountIds(Collections.singletonList(createResponse.nodeId))
                    .setScheduleId(loopReceipt.scheduleId)
                    .execute(loopClient);

                TransactionReceipt signReceipt = new TransactionReceiptQuery()
                    .setTransactionId(signTransaction.transactionId)
                    .execute(client);
                if (signReceipt.status != Status.SUCCESS && signReceipt.status != Status.SCHEDULE_ALREADY_EXECUTED) {
                    throw new Exception("Bad status while getting receipt of schedule sign with operator " + operatorId + ": " + signReceipt.status);
                }
            }
        }

        System.out.println(new ScheduleInfoQuery().setScheduleId(scheduleID).execute(client));

        /*
         * Clean up:
         * Delete created accounts and close created clients.
         */
        AccountDeleteTransaction thresholdDeleteTx = new AccountDeleteTransaction()
            .setAccountId(thresholdAccount)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client);

        for (int i = 0; i < 3; i++) {
            thresholdDeleteTx.sign(privateKeys[i]);
            new AccountDeleteTransaction()
                .setAccountId(accounts[i])
                .setTransferAccountId(OPERATOR_ID)
                .freezeWith(client)
                .sign(privateKeys[i])
                .execute(client)
                .getReceipt(client);
        }

        thresholdDeleteTx
            .execute(client)
            .getReceipt(client);

        client.close();

        for (Client loopClient : clients) {
            loopClient.close();
        }

        System.out.println("Example complete!");
    }
}
