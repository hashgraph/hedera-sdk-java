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
import java.util.Objects;

public final class MultiSigOfflineExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private MultiSigOfflineExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        PrivateKey user1PrivateKey = PrivateKey.generateED25519();
        PublicKey user1PublicKey = user1PrivateKey.getPublicKey();
        PrivateKey user2PrivateKey = PrivateKey.generateED25519();
        PublicKey user2PublicKey = user2PrivateKey.getPublicKey();

        System.out.println("private key for user 1 = " + user1PrivateKey);
        System.out.println("public key for user 1 = " + user1PublicKey);
        System.out.println("private key for user 2 = " + user2PrivateKey);
        System.out.println("public key for user 2 = " + user2PublicKey);

        // create a multi-sig account
        KeyList keylist = new KeyList();
        keylist.add(user1PublicKey);
        keylist.add(user2PublicKey);

        TransactionResponse createAccountTransaction = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(2))
            .setKey(keylist)
            .execute(client);

        @Var
        TransactionReceipt receipt = createAccountTransaction.getReceipt(client);
        var newAccountId = receipt.accountId;

        System.out.println("account id = " + newAccountId);

        // create a transfer from new account to 0.0.3
        TransferTransaction transferTransaction = new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
            .addHbarTransfer(Objects.requireNonNull(receipt.accountId), Hbar.from(-1))
            .addHbarTransfer(new AccountId(3), new Hbar(1))
            .freezeWith(client);

        // convert transaction to bytes to send to signatories
        byte[] transactionBytes = transferTransaction.toBytes();
        Transaction<?> transactionToExecute = Transaction.fromBytes(transactionBytes);

        // ask users to sign and return signature
        byte[] user1Signature = user1PrivateKey.signTransaction(Transaction.fromBytes(transactionBytes));
        byte[] user2Signature = user2PrivateKey.signTransaction(Transaction.fromBytes(transactionBytes));

        // recreate the transaction from bytes
        transactionToExecute.signWithOperator(client);
        transactionToExecute.addSignature(user1PrivateKey.getPublicKey(), user1Signature);
        transactionToExecute.addSignature(user2PrivateKey.getPublicKey(), user2Signature);

        TransactionResponse result = transactionToExecute.execute(client);
        receipt = result.getReceipt(client);
        System.out.println(receipt.status);

        // Clean up
        new AccountDeleteTransaction()
            .setAccountId(newAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(user1PrivateKey)
            .sign(user2PrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();
    }
}
