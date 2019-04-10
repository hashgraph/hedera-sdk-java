package com.hedera.sdk.examples;

import com.hedera.sdk.*;
import com.hedera.sdk.account.AccountCreateTransaction;
import com.hedera.sdk.account.AccountUpdateTransaction;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Map;
import java.util.Objects;

// Ignore duplicate warnings since many examples will look similar
@SuppressWarnings("Duplicates")
public final class UpdateAccountPublicKey {
    public static void main(String[] args) throws InterruptedException {
        var env = Dotenv.load();

        var operatorKey = Ed25519PrivateKey.fromString(Objects.requireNonNull(env.get("OPERATOR_SECRET")));
        var network = Objects.requireNonNull(env.get("NETWORK"));
        var node = AccountId.fromString(Objects.requireNonNull(env.get("NODE")));

        var client = new Client(Map.of(node, network));

        // First we create a new account so we don't affect our account
        var originalKey = Ed25519PrivateKey.generate();

        var txId = new TransactionId(new AccountId(2));
        var tx = new AccountCreateTransaction(client).setTransactionId(txId)
            .setNodeAccount(new AccountId(3))
            .setKey(originalKey.getPublicKey())
            .sign(operatorKey);

        var res = tx.execute();

        // Sleep for 4 seconds
        Thread.sleep(4000);

        var query = new TransactionReceiptQuery(client).setTransaction(txId);

        var receipt = query.execute();
        var receiptStatus = receipt.getStatus();

        var newAccountId = receipt.getAccountId();
        assert newAccountId != null;

        // Now we update the key
        var newKey = Ed25519PrivateKey.generate();
        txId = new TransactionId(new AccountId(6));
        tx = new AccountUpdateTransaction(client).setTransactionId(txId)
            .setNodeAccount(new AccountId(3))
            .setAccountForUpdate(newAccountId)
            .setKey(newKey.getPublicKey())
            // sign as the transaction payer
            .sign(operatorKey)
            // sign as the owner of the account
            .sign(originalKey);

        res = tx.execute();

        System.out.println("transaction: " + res.toString());

        // Sleep for 4 seconds
        Thread.sleep(4000);

        query = new TransactionReceiptQuery(client).setTransaction(txId);

        receipt = query.execute();
        receiptStatus = receipt.getStatus();

        System.out.println("status: " + receiptStatus.toString());
    }
}
