package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.HederaThrowable;
import com.hedera.hashgraph.sdk.TransactionRecord;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class TransferCrypto2 {
    private TransferCrypto2() { }

    private static final int CONCURRENT_TRANSACTIONS = 10;

    public static void main(String[] args) throws HederaException, InterruptedException {
        var operatorId = ExampleHelper.getOperatorId();
        var client = ExampleHelper.createHederaClient();
        var recipientId = ExampleHelper.getNodeId();
        var amount = 1;

        var latch = new CountDownLatch(CONCURRENT_TRANSACTIONS);

        var senderBalanceBefore = client.getAccountBalance(operatorId);
        var receiptBalanceBefore = client.getAccountBalance(recipientId);
        System.out.println("" + operatorId + " balance = " + senderBalanceBefore + " , " +
            recipientId + " balance = " + receiptBalanceBefore);

        Consumer<TransactionRecord> onSuccess = (record) -> {
            System.out.println(System.currentTimeMillis() + " " + record.getMemo() + " completed " +
                record.getConsensusTimestamp());
            latch.countDown();
        };

        Consumer<HederaThrowable> onError = (e) -> {
            System.out.println(e);
            if (e instanceof HederaException) {
                var he = (HederaException)e;
                // System.out.println(he.getMessage());
                System.out.println(he.responseCode);
                // he.printStackTrace();
            }
            latch.countDown();
        };

        for (var i = 0; i < CONCURRENT_TRANSACTIONS; ++i) {
            var memo = "async-test-" + System.currentTimeMillis(); // i;

            var cryptoTransfer = new CryptoTransferTransaction(client)
                .addSender(operatorId, amount)
                .addRecipient(recipientId, amount)
                .setMemo(memo)
                .build();

            System.out.println(cryptoTransfer.getId().getValidStart() + " " + memo + " sent.");
            cryptoTransfer.executeForRecordAsync(onSuccess, onError);
            //Thread.sleep(5);
        }
        try {
            latch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        var senderBalanceAfter = client.getAccountBalance(operatorId);
        var receiptBalanceAfter = client.getAccountBalance(recipientId);
        System.out.println("" + operatorId + " balance = " + senderBalanceAfter + " , " +
            recipientId + " balance = " + receiptBalanceAfter);
    }
}
