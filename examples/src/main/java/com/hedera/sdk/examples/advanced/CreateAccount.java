package com.hedera.sdk.examples.advanced;

import com.hedera.sdk.*;
import com.hedera.sdk.account.AccountCreateTransaction;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.examples.ExampleHelper;

import java.util.Objects;

public final class CreateAccount {
    public static void main(String[] args) throws HederaException {
        // Generate a Ed25519 private, public key pair
        var newKey = Ed25519PrivateKey.generate();
        var newPublicKey = newKey.getPublicKey();

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        var client = ExampleHelper.createHederaClient();

        var tx = new AccountCreateTransaction(client)
            // The only _required_ property here is `key`
            .setKey(newKey.getPublicKey())
            .setInitialBalance(1000);

        // This will wait for the receipt to become available
        var receipt = tx.executeForReceipt();

        var newAccountId = Objects.requireNonNull(receipt.getAccountId());

        System.out.println("account = " + newAccountId);
    }
}
