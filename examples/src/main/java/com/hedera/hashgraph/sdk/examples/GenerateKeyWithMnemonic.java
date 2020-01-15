package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.crypto.Mnemonic;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;

public final class GenerateKeyWithMnemonic {
    private GenerateKeyWithMnemonic() { }

    public static void main(String[] args) {
        Mnemonic mnemonic = Mnemonic.generate();

        Ed25519PrivateKey newKey = mnemonic.toPrivateKey();
        Ed25519PublicKey newPublicKey = newKey.publicKey;

        System.out.println("mnemonic = " + mnemonic);
        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);
    }
}
