package com.hedera.sdk.examples.advanced;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.sdk.Client;
import com.hedera.sdk.HederaException;
import com.hedera.sdk.Transaction;
import com.hedera.sdk.account.AccountCreateTransaction;
import com.hedera.sdk.account.CryptoTransferTransaction;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.examples.ExampleHelper;

public class MultiAppTransfer {
    // the exchange should possess this key, we're only generating it for demonstration purposes
    private static final Ed25519PrivateKey exchangeKey = Ed25519PrivateKey.generate();

    // this is the only key we should actually possess
    private static final Ed25519PrivateKey userKey = Ed25519PrivateKey.generate();

    private static final Client client = ExampleHelper.createHederaClient();

    public static void main(String[] args) throws HederaException, InvalidProtocolBufferException {
        var transferAmount = 10_000;

        // the exchange creates an account for the user to transfer funds to
        var exchangeAccountReceipt = new AccountCreateTransaction(client).setKey(exchangeKey.getPublicKey())
            // the exchange only accepts transfers that it validates through a side channel (e.g. REST API)
            .setReceiverSignatureRequired(true)
            .executeForReceipt();

        var exchangeAccountId = exchangeAccountReceipt.getAccountId();

        // assume the user has an account on the hashgraph with funds already
        var userAccountId = client.createAccount(userKey.getPublicKey(), client.getMaxTransactionFee() + transferAmount);

        var transferTxn = new CryptoTransferTransaction(client).addSender(userAccountId, transferAmount)
            .addRecipient(exchangeAccountId, transferAmount)
            // the exchange-provided memo required to validate the transaction
            .setMemo("https://some-exchange.com/user1/account1")
            .sign(userKey);

        // the exchange must sign the transaction in order for it to be accepted by the network
        // assume this is some REST call to the exchange API server
        var signedTxnBytes = exchangeSignsTransaction(transferTxn.toBytes());

        // we execute the signed transaction and wait for it to be accepted
        Transaction.fromBytes(client, signedTxnBytes)
            .executeForReceipt();

        System.out.println("transferred " + transferAmount + "...");

        var senderBalanceAfter = client.getAccountBalance(userAccountId);
        var receiptBalanceAfter = client.getAccountBalance(exchangeAccountId);

        System.out.println("" + userAccountId + " balance = " + senderBalanceAfter);
        System.out.println("" + exchangeAccountId + " balance = " + receiptBalanceAfter);
    }

    private static byte[] exchangeSignsTransaction(byte[] transactionData) throws InvalidProtocolBufferException {
        return Transaction.fromBytes(client, transactionData)
            .sign(exchangeKey)
            .toBytes();
    }
}
