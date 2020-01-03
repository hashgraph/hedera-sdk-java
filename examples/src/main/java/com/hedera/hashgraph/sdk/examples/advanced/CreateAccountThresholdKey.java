package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.ThresholdKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.cdimascio.dotenv.Dotenv;

public final class CreateAccountThresholdKey {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

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

        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        Transaction tx = new AccountCreateTransaction()
            // require 2 of the 3 keys we generated to sign on anything modifying this account
            .setKey(new ThresholdKey(2).addAll(pubKeys))
            .setInitialBalance(Hbar.fromTinybar(1000))
            .build(client);

        tx.execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = tx.getReceipt(client);

        AccountId newAccountId = receipt.getAccountId();

        System.out.println("account = " + newAccountId);

        TransactionId tsfrTxnId = new CryptoTransferTransaction()
            .addSender(newAccountId, Hbar.of(10))
            .addRecipient(AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID"))), Hbar.of(10))
            // To manually sign, you must explicitly build the Transaction
            .build(client)
            // we sign with 2 of the 3 keys
            .sign(keys.get(0))
            .sign(keys.get(1))
            .execute(client);

        // (important!) wait for the transfer to go to consensus
        tsfrTxnId.getReceipt(client);

        Long balanceAfter = new AccountBalanceQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("account balance after transfer: " + balanceAfter);
    }
}
