/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hiero.sdk.examples;

import com.hiero.sdk.Mnemonic;
import com.hiero.sdk.PrivateKey;
import com.hiero.sdk.PublicKey;

/**
 * How to generate ED25519 key with mnemonic phrase.
 */
class GenerateKeyWithMnemonicExample {

    public static void main(String[] args) {
        System.out.println("Generate ED25519 Key With Mnemonic Phrase Example Start!");

        System.out.println("Generating random 24-word mnemonic from the BIP-39 standard English word list...");
        Mnemonic mnemonic24 = Mnemonic.generate24();
        System.out.println("Generated 24-word mnemonic: " + mnemonic24);

        System.out.println("Recovering an ED25519 private key from the 24-word mnemonic phrase above...");
        PrivateKey privateKey24 = mnemonic24.toStandardEd25519PrivateKey("", 0);
        System.out.println("Recovered ED25519 private key: " + privateKey24);

        System.out.println("Deriving a public key from the above private key...");
        PublicKey publicKey24 = privateKey24.getPublicKey();
        System.out.println("Public key: " + publicKey24);

        System.out.println("---");

        System.out.println("Generating random 12-word mnemonic from the BIP-39 standard English word list...");
        Mnemonic mnemonic12 = Mnemonic.generate12();
        System.out.println("Generated 12-word mnemonic: " + mnemonic12);

        System.out.println("Recovering an ED25519 private key from the 12-word mnemonic phrase above...");
        PrivateKey privateKey12 = mnemonic12.toStandardEd25519PrivateKey("", 0);
        System.out.println("Recovered ED25519 private key: " + privateKey12);

        System.out.println("Deriving a public key from the above private key...");
        PublicKey publicKey12 = privateKey12.getPublicKey();
        System.out.println("Public key: " + publicKey12);

        System.out.println("Generate ED25519 Key With Mnemonic Phrase Example Complete!");
    }
}
