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
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * How to schedule a transaction with a multi-sig account.
 */
class ScheduleMultiSigTransactionExample {

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

        PublicKey operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * Generate 3 ED25519 private keys.
         */
        PrivateKey key1 = PrivateKey.generateED25519();
        PrivateKey key2 = PrivateKey.generateED25519();
        PrivateKey key3 = PrivateKey.generateED25519();

        /*
         * Step 2:
         * Create a Key List from keys generated in previous step. This key will be used as the new account's key
         * The reason we want to use a `KeyList` is to simulate a multi-party system where
         * multiple keys are required to sign.
         */
        KeyList keyList = new KeyList();

        keyList.add(key1.getPublicKey());
        keyList.add(key2.getPublicKey());
        keyList.add(key3.getPublicKey());

        System.out.println("key1 private = " + key1);
        System.out.println("key1 public = " + key1.getPublicKey());
        System.out.println("key1 private = " + key2);
        System.out.println("key2 public = " + key2.getPublicKey());
        System.out.println("key1 private = " + key3);
        System.out.println("key3 public = " + key3.getPublicKey());
        System.out.println("keyList = " + keyList);

        /*
         * Step 3:
         * Create a new account with a Key List created in a previous step.
         */
        TransactionResponse response = new AccountCreateTransaction()
            .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
            // The only _required_ property here is `key`.
            .setKey(keyList)
            .setInitialBalance(new Hbar(10))
            .execute(client);

        // This will wait for the receipt to become available.
        @Var TransactionReceipt receipt = response.getReceipt(client);

        AccountId accountId = Objects.requireNonNull(receipt.accountId);

        System.out.println("accountId = " + accountId);

        /*
         * Step 4:
         * Create a new scheduled transaction for transferring Hbars.
         */
        // Generate a `TransactionId`. This id is used to query the inner scheduled transaction
        // after we expect it to have been executed.
        TransactionId transactionId = TransactionId.generate(OPERATOR_ID);

        System.out.println("transactionId for scheduled transaction = " + transactionId);

        // Create a transfer transaction with 2/3 signatures.
        @Var TransferTransaction transfer = new TransferTransaction()
            .addHbarTransfer(accountId, new Hbar(1).negated())
            .addHbarTransfer(OPERATOR_ID, new Hbar(1));

        // Schedule the transaction.
        ScheduleCreateTransaction scheduled = transfer.schedule()
            .setPayerAccountId(OPERATOR_ID)
            .setAdminKey(operatorPublicKey)
            .freezeWith(client)
            .sign(key2);

        receipt = scheduled.execute(client).getReceipt(client);

        // Get the schedule ID from the receipt
        ScheduleId scheduleId = Objects.requireNonNull(receipt.scheduleId);

        System.out.println("scheduleId = " + scheduleId);

        /*
         * Step 5:
         * Get the schedule info to see if `signatories` is populated with 2/3 signatures.
         */
        ScheduleInfo info = new ScheduleInfoQuery()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setScheduleId(scheduleId)
            .execute(client);

        System.out.println("Schedule Info = " + info);

        transfer = (TransferTransaction) info.getScheduledTransaction();

        Map<AccountId, Hbar> transfers = transfer.getHbarTransfers();

        // Make sure the transfer transaction is what we expect.
        if (transfers.size() != 2) {
            throw new Exception("more transfers than expected");
        }

        if (!transfers.get(accountId).equals(new Hbar(1).negated())) {
            throw new Exception("transfer for " + accountId + " is not what is expected " + transfers.get(accountId));
        }

        if (!transfers.get(OPERATOR_ID).equals(new Hbar(1))) {
            throw new Exception("transfer for " + OPERATOR_ID + " is not what is expected " + transfers.get(OPERATOR_ID));
        }

        System.out.println("sending schedule sign transaction");

        /*
         * Step 6:
         * Send this last signature to Hedera. This last signature _should_ mean the transaction executes
         * since all 3 signatures have been provided.
         */
        new ScheduleSignTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setScheduleId(scheduleId)
            .freezeWith(client)
            .sign(key3)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 7:
         * Query the schedule info again.
         */
        new ScheduleInfoQuery()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setScheduleId(scheduleId)
            .execute(client);

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
            .setAccountId(accountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(key1)
            .sign(key2)
            .sign(key3)
            .execute(client);

        client.close();

        System.out.println("Example complete!");
    }
}
