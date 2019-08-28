package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

public final class CreateAccount {
    private CreateAccount() { }

    public static void main(String[] args) throws HederaException {
        // Generate a Ed25519 private, public key pair
        Ed25519PrivateKey newKey = Ed25519PrivateKey.generate();
        Ed25519PublicKey newPublicKey = newKey.getPublicKey();

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        Client client = ExampleHelper.createHederaClient();

        AccountCreateTransaction tx = new AccountCreateTransaction(client)
            // The only _required_ property here is `key`
            .setKey(newKey.getPublicKey())
            .setInitialBalance(1000)
            .setTransactionFee(10_000_000);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = tx.executeForReceipt();

        AccountId newAccountId = receipt.getAccountId();

        System.out.println("account = " + newAccountId);
    }
}
