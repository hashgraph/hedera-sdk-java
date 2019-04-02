package com.hedera.sdk.examples;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.TransactionGetReceiptQuery;
import com.hedera.sdk.TransactionId;
import com.hedera.sdk.account.AccountUpdateTransaction;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

// Ignore duplicate warnings since many examples will look similar
@SuppressWarnings("Duplicates")
public final class UpdateAccountPublicKey {
    public static void main(String[] args) throws InterruptedException {
        var env = Dotenv.load();

        var operatorKey = Ed25519PrivateKey.fromString(env.get("OPERATOR_SECRET"));
        var newKey = Ed25519PrivateKey.generate();

        var client = new Client(env.get("NETWORK"));

        var txId = new TransactionId(new AccountId(2));
        var tx = new AccountUpdateTransaction()
            .setTransactionId(txId)
            .setNodeAccountId(new AccountId(3))
            .setAccountforUpdate(new AccountId(4))
            .setKey(newKey.getPublicKey())
            .sign(operatorKey);

        var res = tx.execute(client);

        System.out.println("transaction: " + res.toString());

        // Sleep for 4 seconds
        // TODO: We should make the query here retry internally if its "not ready" or "busy"
        Thread.sleep(4000);

        var query = new TransactionGetReceiptQuery()
            .setTransaction(txId);

        var receipt = query.execute(client);
        var receiptStatus = receipt.getReceipt().getStatus();

        System.out.println("status: " + receiptStatus.toString());
    }
}
