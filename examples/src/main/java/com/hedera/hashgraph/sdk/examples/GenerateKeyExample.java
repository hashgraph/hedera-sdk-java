// SPDX-License-Identifier: Apache-2.0
package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;

/**
 * How to generate ED25519 key pair.
 */
class GenerateKeyExample {

    public static void main(String[] args) {
        System.out.println("Generate ED25519 Private And Public Key Pair Example Start!");

        System.out.println("Generating the ED25519 private key...");
        PrivateKey privateKey = PrivateKey.generateED25519();
        System.out.println("Private Key: " + privateKey);

        System.out.println("Deriving a public key from the above private key");
        PublicKey publicKey = privateKey.getPublicKey();
        System.out.println("Public key: " + publicKey);

        System.out.println("Generate ED25519 Private And Public Key Pair Example Complete!");
    }
}
