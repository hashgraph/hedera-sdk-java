package com.hedera.hashgraph.sdk.examples.simple;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
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

        Transaction transaction = new AccountCreateTransaction(client)
            .setMaxTransactionFee(1_000_000_000)
            .setKey(newPublicKey)
            .setInitialBalance(100_000_000)
            .build();

        transaction.execute();

        System.out.println("transaction ID: " + transaction.execute());
        AccountId newAccountId = transaction.queryReceipt().getAccountId();
        System.out.println("account = " + newAccountId);
    }
}
