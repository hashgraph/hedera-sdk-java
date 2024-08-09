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
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

/**
 * HIP-583: Expand alias support in CryptoCreate & CryptoTransfer Transactions.
 * How to transfer Hbar or tokens to a Hedera account using their public-address.
 */
class TransferUsingEvmAddressExample {

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
         * Create an ECSDA private key.
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
        System.out.println("Corresponding evm address: " + evmAddress);

        /*
         * Step 4:
         * Transfer tokens using the `TransferTransaction` to the Etherum Account Address.
         *    - the `from` field should be a complete account that has a public address;
         *    - the `to` field should be to a public address (to create a new account).
         */
        TransferTransaction transferTx = new TransferTransaction()
            .addHbarTransfer(OPERATOR_ID, Hbar.from(10).negated())
            .addHbarTransfer(evmAddress, Hbar.from(10))
            .freezeWith(client);

        TransferTransaction transferTxSign = transferTx.sign(OPERATOR_KEY);
        TransactionResponse transferTxSubmit = transferTxSign.execute(client);

        /*
         * Step 5:
         * Get the child receipt or child record to return the Hedera Account ID for the new account that was created.
         */
        TransactionReceipt receipt = new TransactionReceiptQuery()
            .setTransactionId(transferTxSubmit.transactionId)
            .setIncludeChildren(true)
            .execute(client);

        AccountId newAccountId = receipt.children.get(0).accountId;
        System.out.println(newAccountId);

        /*
         * Step 6:
         * Get the `AccountInfo` on the new account and show it is a hollow account by not having a public key.
         */
        AccountInfo accountInfo = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("accountInfo: " + accountInfo);

        /*
         * Step 7:
         * Use the hollow account as a transaction fee payer in a HAPI transaction.
         */
        client.setOperator(newAccountId, privateKey);
        PrivateKey newPrivateKey = PrivateKey.generateED25519();
        PublicKey newPublicKey = newPrivateKey.getPublicKey();

        AccountCreateTransaction transaction = new AccountCreateTransaction()
            .setKey(newPublicKey)
            .freezeWith(client);

        /*
         * Step 8:
         * Sign the transaction with ECDSA private key.
         */
        AccountCreateTransaction transactionSign = transaction.sign(privateKey);
        TransactionResponse transactionSubmit = transactionSign.execute(client);
        TransactionReceipt status = transactionSubmit.getReceipt(client);
        var accountId = status.accountId;
        System.out.println(status);

        /*
         * Step 9:
         * Get the `AccountInfo` of the account and show the account is now a complete account by returning the public key on the account.
         */
        AccountInfo accountInfo2 = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("The public key of the newly created and now complete account: " + accountInfo2.key);

        /*
         * Clean up:
         * Delete created accounts.
         */
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        new AccountDeleteTransaction()
            .setAccountId(newAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(privateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(accountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(newPrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
