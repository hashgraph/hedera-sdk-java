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
 * How to transfer Hbar to an account with the receiver signature enabled.
 */
class MultiAppTransferExample {

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
        // The exchange should possess this key, we're only generating it for demonstration purposes.
        PrivateKey exchangePrivateKey = PrivateKey.generateED25519();
        PublicKey exchangePublicKey = exchangePrivateKey.getPublicKey();

        // This is the only key we should actually possess.
        PrivateKey userPrivateKey = PrivateKey.generateED25519();
        PublicKey userPublicKey = userPrivateKey.getPublicKey();

        /*
         * Step 2:
         * Create exchange and receiver accounts.
         */
        // The exchange creates an account for the user to transfer funds to.
        AccountId exchangeAccountId = Objects.requireNonNull(
            new AccountCreateTransaction()
            // The exchange only accepts transfers that it validates through a side channel (e.g. REST API).
            .setReceiverSignatureRequired(true)
            .setKey(exchangePublicKey)
            // The owner key has to sign this transaction when setReceiverSignatureRequired is true.
            .freezeWith(client)
            .sign(exchangePrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId
        );

        // For the purpose of this example we create an account for the user with a balance of 5 Hbar.
        AccountId userAccountId = Objects.requireNonNull(
            new AccountCreateTransaction()
            .setInitialBalance(new Hbar(5))
            .setKey(userPublicKey)
            .execute(client)
            .getReceipt(client)
            .accountId
        );

        /*
         * Step 3:
         * Make a transfer from the user account to the exchange account, this requires signing by both parties.
         */
        TransferTransaction transferTxn = new TransferTransaction()
            .addHbarTransfer(userAccountId, new Hbar(2).negated())
            .addHbarTransfer(exchangeAccountId, new Hbar(2))
            // The exchange-provided memo required to validate the transaction.
            .setTransactionMemo("https://some-exchange.com/user1/account1")
            // NOTE: to manually sign, you must freeze the Transaction first
            .freezeWith(client)
            .sign(userPrivateKey);

        // The exchange must sign the transaction in order for it to be accepted by the network
        // (assume this is some REST call to the exchange API server).
        byte[] signedTxnBytes = Transaction.fromBytes(transferTxn.toBytes()).sign(exchangePrivateKey).toBytes();

        // Parse the transaction bytes returned from the exchange.
        Transaction<?> signedTransferTxn = Transaction.fromBytes(signedTxnBytes);

        // Get the amount we are about to transfer (we built this with +2, -2).
        Hbar transferAmount = ((TransferTransaction) signedTransferTxn).getHbarTransfers().values().toArray(new Hbar[0])[0];

        System.out.println("about to transfer " + transferAmount + "...");

        // We now execute the signed transaction and wait for it to be accepted.
        TransactionResponse transactionResponse = signedTransferTxn.execute(client);

        // (Important!) Wait for consensus by querying for the receipt.
        transactionResponse.getReceipt(client);

        /*
         * Step 4:
         * Query user and exchange account balance to validate the transfer was successfully complete.
         */
        Hbar senderBalanceAfter = new AccountBalanceQuery()
            .setAccountId(userAccountId)
            .execute(client)
            .hbars;

        Hbar receiptBalanceAfter = new AccountBalanceQuery()
            .setAccountId(exchangeAccountId)
            .execute(client)
            .hbars;

        System.out.println("" + userAccountId + " balance = " + senderBalanceAfter);
        System.out.println("" + exchangeAccountId + " balance = " + receiptBalanceAfter);

        /*
         * Clean up:
         * Delete created accounts.
         */
        new AccountDeleteTransaction()
            .setAccountId(exchangeAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(exchangePrivateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(userAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(userPrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
