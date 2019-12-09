package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionRecord;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public final class TransferCrypto {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId NODE_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID")));
    private static final String NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("NODE_ADDRESS"));
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private TransferCrypto() { }

    public static void main(String[] args) throws HederaException {
        // To improve responsiveness, you should specify multiple nodes using the
        // `Client(<Map<AccountId, String>>)` constructor instead
        Client client = new Client(NODE_ID, NODE_ADDRESS);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        AccountId recipientId = AccountId.fromString("0.0.3");
        int amount = 10_000;

        long senderBalanceBefore = client.getAccountBalance(OPERATOR_ID);
        long receiptBalanceBefore = client.getAccountBalance(recipientId);

        System.out.println("" + OPERATOR_ID + " balance = " + senderBalanceBefore);
        System.out.println("" + recipientId + " balance = " + receiptBalanceBefore);

        Transaction transaction = new CryptoTransferTransaction()
            // .addSender and .addRecipient can be called as many times as you want as long as the total sum from
            // both sides is equivalent
            .addSender(OPERATOR_ID, amount)
            .addRecipient(recipientId, amount)
            .setMemo("transfer test")
            .build(client);

        System.out.println("transaction ID: " + transaction.id);

        transaction.execute(client);
        TransactionRecord record = transaction.getRecord(client);

        System.out.println("transferred " + amount + "...");

        long senderBalanceAfter = client.getAccountBalance(OPERATOR_ID);
        long receiptBalanceAfter = client.getAccountBalance(recipientId);

        System.out.println("" + OPERATOR_ID + " balance = " + senderBalanceAfter);
        System.out.println("" + recipientId + " balance = " + receiptBalanceAfter);
        System.out.println("Transfer memo: " + record.getMemo());
    }
}
