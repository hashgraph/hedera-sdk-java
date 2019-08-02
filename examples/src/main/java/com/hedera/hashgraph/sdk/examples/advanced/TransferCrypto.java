package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

public final class TransferCrypto {
    private TransferCrypto() { }

    public static void main(String[] args) throws HederaException {
        var client = ExampleHelper.createHederaClient();

        var senderId = AccountId.fromString("0.0.2");
        var recipientId = ExampleHelper.getOperatorId();
        var amount = 100_000;

        var senderBalanceBefore = client.getAccountBalance(senderId);
        var receiptBalanceBefore = client.getAccountBalance(recipientId);

        System.out.println("" + senderId + " balance = " + senderBalanceBefore);
        System.out.println("" + recipientId + " balance = " + receiptBalanceBefore);

        var record = new CryptoTransferTransaction(client)
            // .addSender and .addRecipient can be called as many times as you want as long as the total sum from
            // both sides is equivalent
            .addSender(senderId, amount)
            .addRecipient(recipientId, amount)
            .setTransactionId(new TransactionId(senderId))
            .setMemo("transfer test")
            .setTransactionFee(10_000_000)
            .sign(Ed25519PrivateKey.fromString("302e020100300506032b65700422042091132178e72057a1d7528025956fe39b0b847f200ab59b2fdd367017f3087137"))
            // As we are sending from the operator we do not need to explicitly sign the transaction
            .executeForRecord();

        System.out.println("transferred " + amount + "...");

        var senderBalanceAfter = client.getAccountBalance(senderId);
        var receiptBalanceAfter = client.getAccountBalance(recipientId);

        System.out.println("" + senderId + " balance = " + senderBalanceAfter);
        System.out.println("" + recipientId + " balance = " + receiptBalanceAfter);
        System.out.println("Transfer memo: " + record.getMemo());
    }
}
