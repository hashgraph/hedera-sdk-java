package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

public final class TransferCrypto {
    private TransferCrypto() { }

    public static void main(String[] args) throws HederaException {
        var operatorId = ExampleHelper.getOperatorId();
        var client = ExampleHelper.createHederaClient();

        var recipientId = AccountId.fromString("0.0.3");
        var amount = 10_000;

        var senderBalanceBefore = client.getAccountBalance(operatorId);
        var receiptBalanceBefore = client.getAccountBalance(recipientId);

        System.out.println("" + operatorId + " balance = " + senderBalanceBefore);
        System.out.println("" + recipientId + " balance = " + receiptBalanceBefore);

        var record = new CryptoTransferTransaction(client)
            // .addSender and .addRecipient can be called as many times as you want as long as the total sum from
            // both sides is equivalent
            .addSender(operatorId, amount)
            .addRecipient(recipientId, amount)
            .setMemo("transfer test")
            // As we are sending from the operator we do not need to explicitly sign the transaction
            .executeForRecord();

        System.out.println("transferred " + amount + "...");

        var senderBalanceAfter = client.getAccountBalance(operatorId);
        var receiptBalanceAfter = client.getAccountBalance(recipientId);

        System.out.println("" + operatorId + " balance = " + senderBalanceAfter);
        System.out.println("" + recipientId + " balance = " + receiptBalanceAfter);
        System.out.println("Transfer memo: " + record.getMemo());
    }
}
