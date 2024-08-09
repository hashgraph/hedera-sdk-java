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
 * How to schedule a transaction with a multi-sig account with a threshold.
 */
class ScheduledTransactionMultiSigThresholdExample {

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
         * Generate four new Ed25519 private and public key pairs.
         */
        PrivateKey[] privateKeys = new PrivateKey[4];
        PublicKey[] publicKeys = new PublicKey[4];
        for (int i = 0; i < 4; i++) {
            PrivateKey key = PrivateKey.generateED25519();
            privateKeys[i] = key;
            publicKeys[i] = key.getPublicKey();
            System.out.println("public key " + (i + 1) + ": " + publicKeys[i]);
            System.out.println("private key " + (i + 1) + ": " + privateKeys[i]);
        }

        /*
         * Step 2:
         * Create a Key List with threshold
         * (require 3 of the 4 keys we generated to sign on anything modifying this account).
         */
        KeyList transactionKey = KeyList.withThreshold(3);
        Collections.addAll(transactionKey, publicKeys);

        /*
         * Step 3:
         * Create a new account with a Key List from previous step.
         */
        TransactionResponse transactionResponse = new AccountCreateTransaction()
            .setKey(transactionKey)
            .setInitialBalance(Hbar.fromTinybars(1))
            .setAccountMemo("3-of-4 multi-sig account")
            .execute(client);

        // This will wait for the receipt to become available.
        TransactionReceipt txAccountCreateReceipt = transactionResponse.getReceipt(client);
        AccountId multiSigAccountId = Objects.requireNonNull(txAccountCreateReceipt.accountId);

        System.out.println("3-of-4 multi-sig account ID: " + multiSigAccountId);

        /*
         * Step 4:
         * Check the balance of the newly created account.
         */
        AccountBalance balance = new AccountBalanceQuery()
            .setAccountId(multiSigAccountId)
            .execute(client);
        System.out.println("Balance of account " + multiSigAccountId + ": " + balance.hbars.toTinybars() + " tinybar.");

        /*
         * Step 5:
         * Schedule crypto transfer from multi-sig account to operator account.
         */
        TransactionResponse transferToSchedule = new TransferTransaction()
            .addHbarTransfer(multiSigAccountId, Hbar.fromTinybars(-1))
            .addHbarTransfer(Objects.requireNonNull(client.getOperatorAccountId()), Hbar.fromTinybars(1))
            .schedule()
            .freezeWith(client)
            // Add first signature.
            .sign(privateKeys[0])
            .execute(client);

        TransactionReceipt txScheduleReceipt = transferToSchedule.getReceipt(client);
        System.out.println("Schedule status: " + txScheduleReceipt.status);
        ScheduleId scheduleId = Objects.requireNonNull(txScheduleReceipt.scheduleId);
        System.out.println("Schedule ID: " + scheduleId);
        TransactionId scheduledTxId = Objects.requireNonNull(txScheduleReceipt.scheduledTransactionId);
        System.out.println("Scheduled tx ID: " + scheduledTxId);

        // Add second signature.
        TransactionResponse txScheduleSign1 = new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(client)
            .sign(privateKeys[1])
            .execute(client);

        TransactionReceipt txScheduleSign1Receipt = txScheduleSign1.getReceipt(client);
        System.out.println("1. ScheduleSignTransaction status: " + txScheduleSign1Receipt.status);

        // Add third signature.
        TransactionResponse txScheduleSign2 = new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(client)
            .sign(privateKeys[2])
            .execute(client);

        TransactionReceipt txScheduleSign2Receipt = txScheduleSign2.getReceipt(client);
        System.out.println("2. ScheduleSignTransaction status: " + txScheduleSign2Receipt.status);

        /*
         * Step 6:
         * Query schedule.
         */
        ScheduleInfo scheduleInfo = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(client);
        System.out.println(scheduleInfo);

        /*
         * Step 7:
         * Query triggered scheduled transaction.
         */
        TransactionRecord recordScheduledTx = new TransactionRecordQuery()
            .setTransactionId(scheduledTxId)
            .execute(client);
        System.out.println(recordScheduledTx);

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
            .setAccountId(multiSigAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(privateKeys[0])
            .sign(privateKeys[1])
            .sign(privateKeys[2])
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
