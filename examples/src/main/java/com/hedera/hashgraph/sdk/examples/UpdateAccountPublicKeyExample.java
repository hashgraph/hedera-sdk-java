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

public final class UpdateAccountPublicKeyExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private UpdateAccountPublicKeyExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        client.setDefaultMaxTransactionFee(new Hbar(10));

        // First, we create a new account so we don't affect our account

        PrivateKey privateKey1 = PrivateKey.generateED25519();
        PublicKey publicKey1 = privateKey1.getPublicKey();
        PrivateKey privateKey2 = PrivateKey.generateED25519();
        PublicKey publicKey2 = privateKey2.getPublicKey();

        TransactionResponse acctTransactionResponse = new AccountCreateTransaction()
            .setKey(publicKey1)
            .setInitialBalance(new Hbar(1))
            .execute(client);

        System.out.println("transaction ID: " + acctTransactionResponse);
        AccountId accountId = Objects.requireNonNull(acctTransactionResponse.getReceipt(client).accountId);
        System.out.println("account = " + accountId);
        System.out.println("key = " + privateKey1.getPublicKey());
        // Next, we update the key

        System.out.println(" :: update public key of account " + accountId);
        System.out.println("set key = " + privateKey2.getPublicKey());

        TransactionResponse accountUpdateTransactionResponse = new AccountUpdateTransaction()
            .setAccountId(accountId)
            .setKey(publicKey2)
            .freezeWith(client)
            // sign with the previous key and the new key
            .sign(privateKey1)
            .sign(privateKey2)
            // execute will implicitly sign with the operator
            .execute(client);

        System.out.println("transaction ID: " + accountUpdateTransactionResponse);

        // (important!) wait for the transaction to complete by querying the receipt
        accountUpdateTransactionResponse.getReceipt(client);

        // Now we fetch the account information to check if the key was changed
        System.out.println(" :: getAccount and check our current key");

        AccountInfo info = new AccountInfoQuery()
            .setAccountId(accountId)
            .execute(client);

        System.out.println("key = " + info.key);

        // Clean up
        new AccountDeleteTransaction()
            .setAccountId(accountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(privateKey2)
            .execute(client)
            .getReceipt(client);

        client.close();
    }
}
