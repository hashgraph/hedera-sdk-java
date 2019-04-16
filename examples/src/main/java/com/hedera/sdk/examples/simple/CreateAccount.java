package com.hedera.sdk.examples.simple;

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
        var newAccountId = client.createAccount(newKey.getPublicKey(), 1000);

        System.out.println("account = " + newAccountId);
    }
}
