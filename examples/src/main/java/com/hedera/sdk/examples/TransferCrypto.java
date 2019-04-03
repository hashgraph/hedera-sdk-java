package com.hedera.sdk.examples;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.TransactionGetReceiptQuery;
import com.hedera.sdk.TransactionId;
import com.hedera.sdk.account.CryptoTransferTransaction;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

// Suppress duplicate warnings due to similar example patterns
@SuppressWarnings("Duplicates")
public final class TransferCrypto {
    public static void main(String[] args) throws InterruptedException {
        var env = Dotenv.load();

        var operator = new AccountId(2);
        var operatorKey = Ed25519PrivateKey.fromString(Objects.requireNonNull(env.get("OPERATOR_SECRET")));

        var client = new Client(env.get("NETWORK"));

        var receiver = new AccountId(3);

        var txId = new TransactionId(new AccountId(3));
        var tx = new CryptoTransferTransaction()
            /// value must be positive for both send and receive
            .addSender(operator, 100000)
            .addRecipient(receiver, 100000)
            // You can also add multiple Senders or Receivers,
            // the total value sent and received must be equal.

            // .addTransfer(<UserId>, <value>)
            // can also be used where UserId will be inferred to be a sender
            // or receiver based on the sign of value.

            // the transaction must be signed by all of the senders
            .sign(operatorKey)
            // and then signed by the account paying for the transaction
            // double signing is required if it is the same account
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

        var newAccountId = receipt.getReceipt().getAccountID();

        // System.out.println("new account num: " + newAccountId.getAccountNum());
    }
}
