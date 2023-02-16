/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
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
import com.hedera.hashgraph.sdk.BadMnemonicException;
import com.hedera.hashgraph.sdk.Mnemonic;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;

class GenerateKeyWithMnemonicExample {
    private GenerateKeyWithMnemonicExample() {
    }

    public static void main(String[] args) {
        Mnemonic mnemonic = Mnemonic.generate24();
        PrivateKey privateKey = mnemonic.toStandardEd25519PrivateKey("", 0);
        PublicKey publicKey = privateKey.getPublicKey();

        Mnemonic mnemonic12 = Mnemonic.generate12();
        PrivateKey privateKey12 = mnemonic12.toStandardEd25519PrivateKey("", 0);
        PublicKey publicKey12 = privateKey12.getPublicKey();

        System.out.println("mnemonic 24 word = " + mnemonic);
        System.out.println("private key = " + privateKey);
        System.out.println("public key = " + publicKey);

        System.out.println("mnemonic 12 word = " + mnemonic12);
        System.out.println("private key = " + privateKey12);
        System.out.println("public key = " + publicKey12);
    }
}
