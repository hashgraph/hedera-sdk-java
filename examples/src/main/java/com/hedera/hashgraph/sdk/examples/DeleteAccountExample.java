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

public final class DeleteAccountExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private DeleteAccountExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Generate a Ed25519 private, public key pair
        PrivateKey newPrivateKey = PrivateKey.generateED25519();
        PublicKey newPublicKey = newPrivateKey.getPublicKey();

        System.out.println("private key = " + newPrivateKey);
        System.out.println("public key = " + newPublicKey);

        TransactionResponse transactionResponse = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(newPublicKey)
            .setInitialBalance(new Hbar(2))
            .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = transactionResponse.getReceipt(client);

        AccountId newAccountId = Objects.requireNonNull(receipt.accountId);

        System.out.println("account = " + newAccountId);

        new AccountDeleteTransaction()
            // note the transaction ID has to use the ID of the account being deleted
            .setTransactionId(TransactionId.generate(newAccountId))
            .setAccountId(newAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(newPrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();
    }
}
