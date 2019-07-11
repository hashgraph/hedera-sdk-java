package com.hedera.hashgraph.sdk.examples.simple;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

public final class CreateAccount {
    private CreateAccount() { }

    public static void main(String[] args) throws HederaException {
        // Generate a Ed25519 private, public key pair
        var newKey = Ed25519PrivateKey.generate();
        var newPublicKey = newKey.getPublicKey();

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        var client = ExampleHelper.createHederaClient();
        var newAccountId = client.createAccount(newKey.getPublicKey(), 10);

        System.out.println("account = " + newAccountId);
    }
}
