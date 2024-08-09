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

import java.time.Instant;
import java.util.Objects;

/**
 * How to schedule a transaction.
 */
class ScheduleExample {

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
         * Generate ED25519 private and public keys for accounts.
         */
        PrivateKey privateKey1 = PrivateKey.generateED25519();
        PublicKey publicKey1 = privateKey1.getPublicKey();
        PrivateKey privateKey2 = PrivateKey.generateED25519();
        PublicKey publicKey2 = privateKey2.getPublicKey();

        System.out.println("private key 1 = " + privateKey1);
        System.out.println("public key 1 = " + publicKey1);
        System.out.println("private key 2 = " + privateKey2);
        System.out.println("public key 2 = " + publicKey2);

        /*
         * Step 1:
         * Create a new Hedera account.
         */
        AccountId newAccountId = new AccountCreateTransaction()
            .setKey(KeyList.of(publicKey1, publicKey2))
            .setInitialBalance(Hbar.fromTinybars(1_000))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(newAccountId);

        System.out.println("new account = " + newAccountId);

        /*
         * Step 2:
         * Schedule a transfer transaction.
         */
        TransactionResponse response = new TransferTransaction()
            .addHbarTransfer(newAccountId, Hbar.from(1).negated())
            .addHbarTransfer(client.getOperatorAccountId(), Hbar.from(1))
            .schedule()
            // Set expiration time to be now + 24 hours
            .setExpirationTime(Instant.now().plusSeconds(24 * 60 * 60))
            // Set wait for expiry to true
            .setWaitForExpiry(true)
            .execute(client);

        System.out.println("scheduled transaction ID = " + response.transactionId);

        ScheduleId scheduleId = Objects.requireNonNull(response.getReceipt(client).scheduleId);
        System.out.println("schedule ID = " + scheduleId);

        TransactionRecord record = response.getRecord(client);
        System.out.println("record = " + record);

        /*
         * Step 3:
         * Sign the schedule transaction with the first key.
         */
        new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(client)
            .sign(privateKey1)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 4:
         * Query the state of a schedule transaction.
         */
        ScheduleInfo info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(client);

        System.out.println("schedule info = " + info);

        /*
         * Step 5:
         * Sign the schedule transaction with the second key.
         */
        new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(client)
            .sign(privateKey2)
            .execute(client)
            .getReceipt(client);

        TransactionId transactionId = response.transactionId;
        String validMirrorTransactionId = transactionId.accountId.toString() + "-" + transactionId.validStart.getEpochSecond() + "-" + transactionId.validStart.getNano();

        // TODO: double check this
        System.out.println("The following link should query the mirror node for the scheduled transaction");
        System.out.println("https://" + HEDERA_NETWORK + ".mirrornode.hedera.com/api/v1/transactions/" + validMirrorTransactionId);

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
            .setAccountId(newAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(privateKey1)
            .sign(privateKey2)
            .execute(client);

        client.close();

        System.out.println("Example complete!");
    }
}
