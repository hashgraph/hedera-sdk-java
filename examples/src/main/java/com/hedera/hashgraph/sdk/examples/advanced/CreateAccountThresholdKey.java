package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.ThresholdKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class CreateAccountThresholdKey {
    private CreateAccountThresholdKey() { }

    public static void main(String[] args) throws HederaException {
        // Generate three new Ed25519 private, public key pairs
        final ArrayList<Ed25519PrivateKey> keys = new ArrayList<Ed25519PrivateKey>(3);
        for (int i = 0; i < 3; i++) {
            keys.add(Ed25519PrivateKey.generate());
        }

        final List<Ed25519PublicKey> pubKeys = keys.stream().map(Ed25519PrivateKey::getPublicKey)
            .collect(Collectors.toList());

        System.out.println("private keys: \n"
            + keys.stream()
            .map(Object::toString)
            .collect(Collectors.joining("\n")));

        Client client = ExampleHelper.createHederaClient();

        AccountCreateTransaction tx = new AccountCreateTransaction(client)
            // require 2 of the 3 keys we generated to sign on anything modifying this account
            .setKey(new ThresholdKey(2).addAll(pubKeys))
            .setInitialBalance(1_000_000)
            .setTransactionFee(10_000_000);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = tx.executeForReceipt();

        AccountId newAccountId = receipt.getAccountId();

        System.out.println("account = " + newAccountId);

        Transaction tsfrTxn = new CryptoTransferTransaction(client)
            .addSender(newAccountId, 50_000)
            .addRecipient(ExampleHelper.getNodeId(), 50_000)
            .setTransactionFee(200_000)
            // we sign with 2 of the 3 keys
            .sign(keys.get(0))
            .sign(keys.get(1));

        // wait for the transfer to go to consensus
        tsfrTxn.executeForReceipt();

        Long balanceAfter = new AccountBalanceQuery(client)
            .setAccountId(newAccountId)
            .execute();

        System.out.println("account balance after transfer: " + balanceAfter);
    }
}
