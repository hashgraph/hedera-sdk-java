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
