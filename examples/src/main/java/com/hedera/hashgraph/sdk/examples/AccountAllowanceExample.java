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
import com.hedera.hashgraph.sdk.logger.LogLevel;
import com.hedera.hashgraph.sdk.logger.Logger;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

/**
 * Approve allowance is a transaction that allows a token owner to delegate a token spender to spend
 * a specified token amount on behalf of the token owner. This can be done for HBAR, non-fungible,
 * and fungible tokens. The owner grants the token allowance to the spender, who can then transfer tokens
 * from the owner's account to another recipient, paying for the transaction fees themselves.
 *
 * On the other hand, delete allowance is a transaction that removes one or more non-fungible
 * approved allowances from an owner's account. This operation removes the allowances granted to
 * specific non-fungible token serial numbers. HBAR and fungible token allowances can be removed by
 * setting the amount to zero in CryptoApproveAllowance.
 */
class AccountAllowanceExample {

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
         * Generate ED25519 private and public keys for accounts.
         */
        PrivateKey alicePrivateKey = PrivateKey.generateED25519();
        PublicKey alicePublicKey = alicePrivateKey.getPublicKey();

        PrivateKey bobPrivateKey = PrivateKey.generateED25519();
        PublicKey bobPublicKey = bobPrivateKey.getPublicKey();

        PrivateKey charliePrivateKey = PrivateKey.generateED25519();
        PublicKey charliePublicKey = charliePrivateKey.getPublicKey();

        /*
         * Step 2:
         * Create accounts for this example.
         */
        System.out.println("Generating accounts for example...");

        AccountId aliceId = new AccountCreateTransaction()
            .setKey(alicePublicKey)
            .setInitialBalance(Hbar.from(5))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(aliceId);

        AccountId bobId = new AccountCreateTransaction()
            .setKey(bobPublicKey)
            .setInitialBalance(Hbar.from(5))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(bobId);

        AccountId charlieId = new AccountCreateTransaction()
            .setKey(charliePublicKey)
            .setInitialBalance(Hbar.from(5))
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(charlieId);

        System.out.println("Alice ID: " + aliceId);
        System.out.println("Bob ID: " + bobId);
        System.out.println("Charlie ID: " + charlieId);

        System.out.println(
            "Alice's balance: " +
                new AccountBalanceQuery().setAccountId(aliceId).execute(client).hbars
        );
        System.out.println(
            "Bob's balance: " +
                new AccountBalanceQuery().setAccountId(bobId).execute(client).hbars
        );
        System.out.println(
            "Charlie's balance: " +
                new AccountBalanceQuery().setAccountId(charlieId).execute(client).hbars
        );

        /*
         * Step 3:
         * Approve an allowance of 2 Hbar with owner Alice and spender Bob.
         */
        System.out.println("Approving an allowance of 2 Hbar with owner Alice and spender Bob");

        new AccountAllowanceApproveTransaction()
            .approveHbarAllowance(aliceId, bobId, Hbar.from(2))
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client)
            .getReceipt(client);

        System.out.println(
            "Alice's balance: " +
                new AccountBalanceQuery().setAccountId(aliceId).execute(client).hbars
        );
        System.out.println(
            "Bob's balance: " +
                new AccountBalanceQuery().setAccountId(bobId).execute(client).hbars
        );
        System.out.println(
            "Charlie's balance: " +
                new AccountBalanceQuery().setAccountId(charlieId).execute(client).hbars
        );

        /*
         * Step 4:
         * Demonstrate allowance -- transfer 1 Hbar from Alice to Charlie, but the transaction is signed _only_ by Bob
         * (Bob is dipping into his allowance from Alice).
         */
        System.out.println("Transferring 1 Hbar from Alice to Charlie, but the transaction is signed _only_ by Bob (Bob is dipping into his allowance from Alice)");

        new TransferTransaction()
            // "addApproved*Transfer()" means that the transfer has been approved by an allowance
            .addApprovedHbarTransfer(aliceId, Hbar.from(1).negated())
            .addHbarTransfer(charlieId, Hbar.from(1))
            // The allowance spender must be pay the fee for the transaction.
            // use setTransactionId() to set the account ID that will pay the fee for the transaction.
            .setTransactionId(TransactionId.generate(bobId))
            .freezeWith(client)
            .sign(bobPrivateKey)
            .execute(client)
            .getReceipt(client);

        System.out.println("Transfer succeeded. Bob should now have 1 Hbar left in his allowance.");

        System.out.println(
            "Alice's balance: " +
                new AccountBalanceQuery().setAccountId(aliceId).execute(client).hbars
        );
        System.out.println(
            "Bob's balance: " +
                new AccountBalanceQuery().setAccountId(bobId).execute(client).hbars
        );
        System.out.println(
            "Charlie's balance: " +
                new AccountBalanceQuery().setAccountId(charlieId).execute(client).hbars
        );

        /*
         * Step 5:
         * Demonstrate the absence of an allowance -- attempt to transfer 2 Hbar from Alice to Charlie using Bob's allowance.
         * This should fail, because there is only 1 Hbar left in Bob's allowance.
         */
        try {
            System.out.println("Attempting to transfer 2 Hbar from Alice to Charlie using Bob's allowance.");
            System.out.println("This should fail, because there is only 1 Hbar left in Bob's allowance.");

            new TransferTransaction()
                .addApprovedHbarTransfer(aliceId, Hbar.from(2).negated())
                .addHbarTransfer(charlieId, Hbar.from(2))
                .setTransactionId(TransactionId.generate(bobId))
                .freezeWith(client)
                .sign(bobPrivateKey)
                .execute(client)
                .getReceipt(client);

            System.out.println("The transfer succeeded.  This should not happen.");

        } catch (Throwable error) {
            System.out.println("The transfer failed as expected.");
            System.out.println(error.getMessage());
        }

        /*
         * Step 6:
         * Demonstrate update of an allowance -- adjust Bob's allowance to 3 Hbar.
         */
        System.out.println("Adjusting Bob's allowance to 3 Hbar.");

        new AccountAllowanceApproveTransaction()
            .approveHbarAllowance(aliceId, bobId, Hbar.from(3))
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 7:
         * Demonstrate allowance -- transfer 2 Hbar from Alice to Charlie using Bob's allowance again.
         */
        System.out.println("Attempting to transfer 2 Hbar from Alice to Charlie using Bob's allowance again.");
        System.out.println("This time it should succeed.");

        new TransferTransaction()
            .addApprovedHbarTransfer(aliceId, Hbar.from(2).negated())
            .addHbarTransfer(charlieId, Hbar.from(2))
            .setTransactionId(TransactionId.generate(bobId))
            .freezeWith(client)
            .sign(bobPrivateKey)
            .execute(client)
            .getReceipt(client);

        System.out.println("Transfer succeeded.");

        System.out.println(
            "Alice's balance: " +
                new AccountBalanceQuery().setAccountId(aliceId).execute(client).hbars
        );
        System.out.println(
            "Bob's balance: " +
                new AccountBalanceQuery().setAccountId(bobId).execute(client).hbars
        );
        System.out.println(
            "Charlie's balance: " +
                new AccountBalanceQuery().setAccountId(charlieId).execute(client).hbars
        );

        /*
         * Clean up:
         * Delete allowance and created accounts.
         */
        System.out.println("Deleting Bob's allowance");

        new AccountAllowanceApproveTransaction()
            .approveHbarAllowance(aliceId, bobId, Hbar.ZERO)
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(aliceId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(bobId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(bobPrivateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(charlieId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(charliePrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
