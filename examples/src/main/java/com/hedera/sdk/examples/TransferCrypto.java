package com.hedera.sdk.examples;

import com.hedera.sdk.account.AccountId;
import com.hedera.sdk.HederaException;
import com.hedera.sdk.account.CryptoTransferTransaction;

// Suppress duplicate warnings due to similar example patterns
@SuppressWarnings("Duplicates")
public final class TransferCrypto {
    public static void main(String[] args) throws HederaException {
        var operatorId = ExampleHelper.getOperatorId();
        var client = ExampleHelper.createHederaClient();

        var recipientId = AccountId.fromString("0.0.3");
        var amount = 10_000;

        var senderBalanceBefore = client.getAccountBalance(operatorId);
        var receiptBalanceBefore = client.getAccountBalance(recipientId);

        System.out.println("" + operatorId + " balance = " + senderBalanceBefore);
        System.out.println("" + recipientId + " balance = " + receiptBalanceBefore);

        new CryptoTransferTransaction(client)
            // .addSender and .addRecipient can be called as many times as you want as long as the total sum from
            // both sides is equivalent
            .addSender(operatorId, amount)
            .addRecipient(recipientId, amount)
            // As we are sending from the operator we do not need to explicitly sign the transaction
            .executeForReceipt();

        System.out.println("transferred " + amount + "...");

        var senderBalanceAfter = client.getAccountBalance(operatorId);
        var receiptBalanceAfter = client.getAccountBalance(recipientId);

        System.out.println("" + operatorId + " balance = " + senderBalanceAfter);
        System.out.println("" + recipientId + " balance = " + receiptBalanceAfter);
    }
}
