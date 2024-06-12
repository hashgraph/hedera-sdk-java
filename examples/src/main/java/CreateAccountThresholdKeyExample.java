/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2023 Hedera Hashgraph, LLC
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
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.Key;
import com.hedera.hashgraph.sdk.KeyList;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.TransferTransaction;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class CreateAccountThresholdKeyExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private CreateAccountThresholdKeyExample() {
    }

    public static void main(String[] args)
        throws PrecheckStatusException, TimeoutException, ReceiptStatusException, InterruptedException {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Generate three new Ed25519 private, public key pairs.
        // You do not need the private keys to create the Threshold Key List,
        // you only need the public keys, and if you're doing things correctly,
        // you probably shouldn't have these private keys.
        PrivateKey[] privateKeys = new PrivateKey[3];
        PublicKey[] publicKeys = new PublicKey[3];
        for (int i = 0; i < 3; i++) {
            PrivateKey key = PrivateKey.generateED25519();
            privateKeys[i] = key;
            publicKeys[i] = key.getPublicKey();
        }

        System.out.println("public keys: ");
        for (Key key : publicKeys) {
            System.out.println(key);
        }

        // require 2 of the 3 keys we generated to sign on anything modifying this account
        KeyList transactionKey = KeyList.withThreshold(2);
        Collections.addAll(transactionKey, publicKeys);

        TransactionResponse transactionResponse = new AccountCreateTransaction()
            .setKey(transactionKey)
            .setInitialBalance(new Hbar(10))
            .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = transactionResponse.getReceipt(client);

        AccountId newAccountId = Objects.requireNonNull(receipt.accountId);

        System.out.println("account = " + newAccountId);

        TransactionResponse transferTransactionResponse = new TransferTransaction()
            .addHbarTransfer(newAccountId, new Hbar(10).negated())
            .addHbarTransfer(new AccountId(3), new Hbar(10))
            // To manually sign, you must explicitly build the Transaction
            .freezeWith(client)
            // we sign with 2 of the 3 keys
            .sign(privateKeys[0])
            .sign(privateKeys[1])
            .execute(client);


        // (important!) wait for the transfer to go to consensus
        transferTransactionResponse.getReceipt(client);

        Hbar balanceAfter = new AccountBalanceQuery()
            .setAccountId(newAccountId)
            .execute(client)
            .hbars;

        System.out.println("account balance after transfer: " + balanceAfter);
    }
}
