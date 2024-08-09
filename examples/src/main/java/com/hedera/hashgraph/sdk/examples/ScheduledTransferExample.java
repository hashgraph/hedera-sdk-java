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

import java.util.Objects;

/**
 * A scheduled transaction is a transaction that has been proposed by an account,
 * but which requires more signatures before it will actually execute on the Hedera network.
 * <p>
 * For example, if Alice wants to transfer an amount of Hbar to Bob, and Bob has
 * `receiverSignatureRequired` set to true, then that transaction must be signed by
 * both Alice and Bob before the transaction will be executed.
 * <p>
 * To solve this problem, Alice can propose the transaction by creating a scheduled
 * transaction on the Hedera network which, if executed, would transfer Hbar from
 * Alice to Bob. That scheduled transaction will have a `ScheduleId` by which we can
 * refer to that scheduled transaction. Alice can communicate the `ScheduleId` to Bob, and
 * then Bob can use a `ScheduleSignTransaction` to sign that scheduled transaction.
 * <p>
 * Bob has a 30-minute window in which to sign the scheduled transaction, starting at the
 * moment that Alice creates the scheduled transaction.  If a scheduled transaction
 * is not signed by all the necessary signatories within the 30-minute window,
 * that scheduled transaction will expire, and will not be executed.
 * <p>
 * Once a scheduled transaction has all the signatures necessary to execute, it will
 * be executed on the Hedera network automatically. If you create a scheduled transaction
 * on the Hedera network, but that transaction only requires your signature in order to
 * execute and no one else's, that scheduled transaction will be automatically
 * executed immediately.
 */
class ScheduledTransferExample {

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
         * Generate ED25519 private and public key pair for Bob's account.
         */
        PrivateKey bobsPrivateKey = PrivateKey.generateED25519();
        PublicKey bobsPublicKey = bobsPrivateKey.getPublicKey();

        /*
         * Step 2:
         * Create Bob's account with receiver signature property enabled.
         */
        AccountId bobsId = new AccountCreateTransaction()
            .setReceiverSignatureRequired(true)
            .setKey(bobsPublicKey)
            .setInitialBalance(new Hbar(10))
            .freezeWith(client)
            .sign(bobsPrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(bobsId);

        /*
         * Step 3:
         * Check Bob's initial balance.
         */
        System.out.println("Alice's ID: " + client.getOperatorAccountId());
        System.out.println("Bob's ID: " + bobsId);

        AccountBalance bobsInitialBalance = new AccountBalanceQuery()
            .setAccountId(bobsId)
            .execute(client);
        System.out.println("Bob's initial balance:");
        System.out.println(bobsInitialBalance);

        /*
         * Step 4:
         * Create a transfer transaction which we will schedule.
         */
        TransferTransaction transferToSchedule = new TransferTransaction()
            .addHbarTransfer(client.getOperatorAccountId(), new Hbar(-1))
            .addHbarTransfer(bobsId, new Hbar(1));
        System.out.println("Transfer to be scheduled:");
        System.out.println(transferToSchedule);

        /*
         * Step 5:
         * Create a scheduled transaction from a transfer transaction.
         *
         * The `payerAccountId` is the account that will be charged the fee
         * for executing the scheduled transaction if/when it is executed.
         * That fee is separate from the fee that we will pay to execute the
         * `ScheduleCreateTransaction` itself.
         *
         * To clarify: Alice pays a fee to execute the `ScheduleCreateTransaction`,
         * which creates the scheduled transaction on the Hedera network.
         * She specifies when creating the scheduled transaction that Bob will pay
         * the fee for the scheduled transaction when it is executed.
         *
         * If `payerAccountId` is not specified, the account who creates the scheduled transaction
         * will be charged for executing the scheduled transaction.
         */
        ScheduleId scheduleId = new ScheduleCreateTransaction()
            .setScheduledTransaction(transferToSchedule)
            .setPayerAccountId(bobsId)
            .execute(client)
            .getReceipt(client)
            .scheduleId;
        Objects.requireNonNull(scheduleId);
        System.out.println("The scheduleId is: " + scheduleId);

        /*
         * Step 6:
         * Check Bob's balance -- it should be unchanged, because the transfer has been scheduled,
         * but it hasn't been executed yet as it requires Bob's signature.
         */
        AccountBalance bobsBalanceAfterSchedule = new AccountBalanceQuery()
            .setAccountId(bobsId)
            .execute(client);
        System.out.println("Bob's balance after scheduling the transfer (should be unchanged):");
        System.out.println(bobsBalanceAfterSchedule);

        /*
         * Step 7:
         * Query the state of a schedule transaction.
         * Once Alice has communicated the scheduleId to Bob, Bob can query for information about the
         * scheduled transaction.
         */
        ScheduleInfo scheduledTransactionInfo = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(client);
        System.out.println("Info about scheduled transaction:");
        System.out.println(scheduledTransactionInfo);

        //`getScheduledTransaction()` will return an SDK Transaction object identical to the transaction
        // that was scheduled, which Bob can then inspect like a normal transaction.
        Transaction<?> scheduledTransaction = scheduledTransactionInfo.getScheduledTransaction();

        // We happen to know that this transaction is (or certainly ought to be) a TransferTransaction.
        if (scheduledTransaction instanceof TransferTransaction) {
            TransferTransaction scheduledTransfer = (TransferTransaction) scheduledTransaction;
            System.out.println("The scheduled transfer transaction from Bob's POV:");
            System.out.println(scheduledTransfer);
        } else {
            throw new Exception("The scheduled transaction was not a transfer transaction.");
        }

        /*
         * Step 8:
         * Appends Bob's signature to a schedule transaction.
         */
        new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(client)
            .sign(bobsPrivateKey)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 9:
         * Check Bob's account balance after signing the scheduled transaction.
         */
        AccountBalance balanceAfterSigning = new AccountBalanceQuery()
            .setAccountId(bobsId)
            .execute(client);
        System.out.println("Bob's balance after signing the scheduled transaction:");
        System.out.println(balanceAfterSigning);

        /*
         * Step 10:
         * Query the state of a schedule transaction.
         */
        ScheduleInfo postTransactionInfo = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(client);
        System.out.println("Info on the scheduled transaction, executedAt should no longer be null:");
        System.out.println(postTransactionInfo);

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
            .setTransferAccountId(client.getOperatorAccountId())
            .setAccountId(bobsId)
            .freezeWith(client)
            .sign(bobsPrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
