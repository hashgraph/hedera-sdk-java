package com.hedera.sdk.examples;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.TransactionId;
import com.hedera.sdk.account.AccountBalanceQuery;
import com.hedera.sdk.account.CryptoTransferTransaction;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Map;
import java.util.Objects;

// Suppress duplicate warnings due to similar example patterns
@SuppressWarnings("Duplicates")
public final class TransferCrypto {
    public static void main(String[] args) throws InterruptedException {
        var env = Dotenv.load();

        var operator = AccountId.fromString(Objects.requireNonNull(env.get("OPERATOR")));
        var operatorKey = Ed25519PrivateKey.fromString(Objects.requireNonNull(env.get("OPERATOR_SECRET")));

        var node = new AccountId(3);

        var client = new Client(
                Map.of(Objects.requireNonNull(env.get("NETWORK")), AccountId.fromString(Objects.requireNonNull(env.get("NODE"))))
        );

        var recipient = AccountId.fromString(Objects.requireNonNull(env.get("Recipient")));

        var txId = new TransactionId(new AccountId(3));
        var tx = new CryptoTransferTransaction().setTransactionId(txId)
            .setNodeAccount(node)
            /// value must be positive for both send and receive
            .addSender(operator, 100000)
            .addRecipient(recipient, 100000)
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

        // Next we get the balance after the transaction
        var txPayment = new CryptoTransferTransaction().setNodeAccount(node)
            .setTransactionId(new TransactionId(node))
            .addSender(operator, 100000)
            .addRecipient(node, 100000)
            .sign(operatorKey);

        var query = new AccountBalanceQuery().setAccount(recipient)
            .setPayment(txPayment);

        var balance = query.execute(client);

        System.out.println(balance.toString());
    }
}
