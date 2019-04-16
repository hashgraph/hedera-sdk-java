package com.hedera.sdk.examples;

import com.hedera.sdk.account.AccountId;
import com.hedera.sdk.HederaException;
import com.hedera.sdk.TransactionId;
import com.hedera.sdk.account.AccountBalanceQuery;
import com.hedera.sdk.account.CryptoTransferTransaction;

// Suppress duplicate warnings due to similar example patterns
@SuppressWarnings("Duplicates")
public final class TransferCrypto {
    public static void main(String[] args) throws InterruptedException {
        var client = ExampleHelper.createHederaClient();

        var recipientId = AccountId.fromString("0.0.3");

        var txId = new TransactionId(new AccountId(3));
        var tx = new CryptoTransferTransaction(client).setTransactionId(txId)
            .setNodeAccountId(node)
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

        try {
            tx.execute();
        } catch (HederaException e) {
            System.out.println("Failed to transfer balance: " + e);
            return;
        }

        // Sleep for 4 seconds
        // TODO: We should make the query here retry internally if its "not ready" or "busy"
        Thread.sleep(4000);

        // Next we get the balance after the transaction
        var txPayment = new CryptoTransferTransaction(client).setNodeAccountId(node)
            .setTransactionId(new TransactionId(node))
            .addSender(operator, 100000)
            .addRecipient(node, 100000)
            .sign(operatorKey);

        var query = new AccountBalanceQuery(client).setAccount(recipient)
            .setPayment(txPayment);

        try {
            var balance = query.execute();
            System.out.println(balance.toString());
        } catch (HederaException e) {
            System.out.println("Failed to get account balance: " + e);
        }
    }
}
