/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
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
import com.google.errorprone.annotations.Var;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.KeyList;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.TransferTransaction;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class MultiSigOfflineExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private MultiSigOfflineExample() {
    }

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException, InvalidProtocolBufferException {

        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        PrivateKey user1Key = PrivateKey.generateED25519();
        PrivateKey user2Key = PrivateKey.generateED25519();

        System.out.println("private key for user 1 = " + user1Key);
        System.out.println("public key for user 1 = " + user1Key.getPublicKey());
        System.out.println("private key for user 2 = " + user2Key);
        System.out.println("public key for user 2 = " + user2Key.getPublicKey());

        // create a multi-sig account
        KeyList keylist = new KeyList();
        keylist.add(user1Key);
        keylist.add(user2Key);

        TransactionResponse createAccountTransaction = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(2))
            .setKey(keylist)
            .execute(client);

        @Var
        TransactionReceipt receipt = createAccountTransaction.getReceipt(client);

        System.out.println("account id = " + receipt.accountId);

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
        byte[] user1Signature = user1Key.signTransaction(Transaction.fromBytes(transactionBytes));
        byte[] user2Signature = user2Key.signTransaction(Transaction.fromBytes(transactionBytes));

        // recreate the transaction from bytes
        transactionToExecute.signWithOperator(client);
        transactionToExecute.addSignature(user1Key.getPublicKey(), user1Signature);
        transactionToExecute.addSignature(user2Key.getPublicKey(), user2Signature);

        TransactionResponse result = transactionToExecute.execute(client);
        receipt = result.getReceipt(client);
        System.out.println(receipt.status);
    }
}
