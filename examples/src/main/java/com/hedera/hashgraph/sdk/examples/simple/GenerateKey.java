package com.hedera.hashgraph.sdk.examples.simple;

import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

public final class GenerateKey {
    private GenerateKey() { }

    public static void main(String[] args) {
        // Generate a Ed25519 private, public key pair
        var newKey = Ed25519PrivateKey.generate();
        var newPublicKey = newKey.getPublicKey();

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);
    }
}
