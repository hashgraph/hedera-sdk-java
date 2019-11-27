package com.hedera.hashgraph.sdk.examples.advanced;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public final class MultiAppTransfer {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId NODE_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID")));
    private static final String NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("NODE_ADDRESS"));
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private MultiAppTransfer() { }

    // the exchange should possess this key, we're only generating it for demonstration purposes
    private static final Ed25519PrivateKey exchangeKey = Ed25519PrivateKey.generate();

    // this is the only key we should actually possess
    private static final Ed25519PrivateKey userKey = Ed25519PrivateKey.generate();

    private static final Client client;

    static {
        // To improve responsiveness, you should specify multiple nodes using the
        // `Client(<Map<AccountId, String>>)` constructor instead
        client = new Client(NODE_ID, NODE_ADDRESS);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
    }

    public static void main(String[] args) throws HederaException, InvalidProtocolBufferException {
        int transferAmount = 10_000;

        // the exchange creates an account for the user to transfer funds to
        TransactionReceipt exchangeAccountReceipt = new AccountCreateTransaction(client)
            // the exchange only accepts transfers that it validates through a side channel (e.g. REST API)
            .setReceiverSignatureRequired(true)
            .setKey(exchangeKey.getPublicKey())
            // The owner key has to sign this transaction
            // when setReceiverSignatureRequired is true
            .sign(exchangeKey)
            .executeForReceipt();

        AccountId exchangeAccountId = exchangeAccountReceipt.getAccountId();

        // assume the user has an account on the hashgraph with funds already
        AccountId userAccountId = client.createAccount(userKey.getPublicKey(), client.getMaxTransactionFee() + transferAmount);

        Transaction transferTxn = new CryptoTransferTransaction(client).addSender(userAccountId, transferAmount)
            .addRecipient(exchangeAccountId, transferAmount)
            // the exchange-provided memo required to validate the transaction
            .setMemo("https://some-exchange.com/user1/account1")
            .sign(userKey);

        // the exchange must sign the transaction in order for it to be accepted by the network
        // assume this is some REST call to the exchange API server
        byte[] signedTxnBytes = exchangeSignsTransaction(transferTxn.toBytes());

        // we execute the signed transaction and wait for it to be accepted
        Transaction.fromBytes(client, signedTxnBytes)
            .executeForReceipt();

        System.out.println("transferred " + transferAmount + "...");

        long senderBalanceAfter = client.getAccountBalance(userAccountId);
        long receiptBalanceAfter = client.getAccountBalance(exchangeAccountId);

        System.out.println("" + userAccountId + " balance = " + senderBalanceAfter);
        System.out.println("" + exchangeAccountId + " balance = " + receiptBalanceAfter);
    }

    private static byte[] exchangeSignsTransaction(byte[] transactionData) throws InvalidProtocolBufferException {
        return Transaction.fromBytes(client, transactionData)
            .sign(exchangeKey)
            .toBytes();
    }
}
