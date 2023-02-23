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
package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ECDSAPublicKeyTest {
    @Test
    void verifyTransaction() {
        var transaction = new TransferTransaction()
                .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
                .setTransactionId(TransactionId.generate(new AccountId(4)))
                .freeze();

        var key = PrivateKey.fromStringECDSA("8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048");
        key.signTransaction(transaction);

        assertThat(key.getPublicKey().verifyTransaction(transaction)).isTrue();
    }

    @Test
    @DisplayName("public key can be recovered from bytes")
    void keyByteSerialization() {
        PublicKey key1 = PrivateKey.generateECDSA().getPublicKey();
        byte[] key1Bytes = key1.toBytes();
        PublicKey key2 = PublicKey.fromBytes(key1Bytes);
        byte[] key2Bytes = key2.toBytes();

        assertThat(key2Bytes).containsExactly(key1Bytes);
    }

    @Test
    @DisplayName("public key can be recovered from raw bytes")
    void keyByteSerialization2() {
        PublicKey key1 = PrivateKey.generateECDSA().getPublicKey();
        byte[] key1Bytes = key1.toBytesRaw();
        PublicKey key2 = PublicKey.fromBytesECDSA(key1Bytes);
        byte[] key2Bytes = key2.toBytesRaw();
        // cannot use PrivateKey.fromBytes() to parse raw ECDSA bytes
        // because they're indistinguishable from ED25519 raw bytes

        assertThat(key2Bytes).containsExactly(key1Bytes);
    }

    @Test
    @DisplayName("public key can be recovered from DER bytes")
    void keyByteSerialization3() {
        PublicKey key1 = PrivateKey.generateECDSA().getPublicKey();
        byte[] key1Bytes = key1.toBytesDER();
        PublicKey key2 = PublicKey.fromBytesDER(key1Bytes);
        byte[] key2Bytes = key2.toBytesDER();
        PublicKey key3 = PublicKey.fromBytes(key1Bytes);
        byte[] key3Bytes = key3.toBytesDER();

        assertThat(key2Bytes).containsExactly(key1Bytes);
        assertThat(key3Bytes).isEqualTo(key1Bytes);
    }

    @Test
    @DisplayName("public key can be recovered from string")
    void keyStringSerialization() {
        PublicKey key1 = PrivateKey.generateECDSA().getPublicKey();
        String key1Str = key1.toString();
        PublicKey key2 = PublicKey.fromString(key1Str);
        String key2Str = key2.toString();
        PublicKey key3 = PublicKey.fromString(key1Str);
        String key3Str = key3.toString();

        assertThat(key3.getClass()).isEqualTo(PublicKeyECDSA.class);
        assertThat(key2Str).isEqualTo(key1Str);
        assertThat(key3Str).isEqualTo(key1Str);
    }

    @Test
    @DisplayName("public key can be recovered from raw string")
    void keyStringSerialization2() {
        PublicKey key1 = PrivateKey.generateECDSA().getPublicKey();
        String key1Str = key1.toStringRaw();
        PublicKey key2 = PublicKey.fromStringECDSA(key1Str);
        String key2Str = key2.toStringRaw();
        PublicKey key3 = PublicKey.fromStringECDSA(key2Str);
        String key3Str = key3.toStringRaw();
        // cannot use PublicKey.fromString() to parse raw ECDSA string
        // because it's indistinguishable from ED25519 raw bytes

        assertThat(key3.getClass()).isEqualTo(PublicKeyECDSA.class);
        assertThat(key2Str).isEqualTo(key1Str);
        assertThat(key3Str).isEqualTo(key1Str);
    }

    @Test
    @DisplayName("public key can be recovered from DER string")
    void keyStringSerialization3() {
        PublicKey key1 = PrivateKey.generateECDSA().getPublicKey();
        String key1Str = key1.toStringDER();
        PublicKey key2 = PublicKey.fromStringDER(key1Str);
        String key2Str = key2.toStringDER();
        PublicKey key3 = PublicKey.fromString(key1Str);
        String key3Str = key3.toStringDER();

        assertThat(key3.getClass()).isEqualTo(PublicKeyECDSA.class);
        assertThat(key2Str).isEqualTo(key1Str);
        assertThat(key3Str).isEqualTo(key1Str);
    }

    @Test
    @DisplayName("public key is is ECDSA")
    void keyIsECDSA() {
        PublicKey key = PrivateKey.generateECDSA().getPublicKey();

        assertThat(key.isECDSA()).isTrue();
    }

    @Test
    @DisplayName("public key is is not Ed25519")
    void keyIsNotEd25519() {
        PublicKey key = PrivateKey.generateECDSA().getPublicKey();

        assertThat(key.isED25519()).isFalse();
    }

    @Test
    @DisplayName("to EVM address")
    void toEvmAddress() {
        // Generated by https://www.rfctools.com/ethereum-address-test-tool/
        String privateKeyString = "DEBAE3CA62AB3157110DBA79C8DE26540DC320EE9BE73A77D70BA175643A3500";
        String expectedEvmAddress = "d8eb8db03c699faa3f47adcdcd2ae91773b10f8b";

        PrivateKey privateKey = PrivateKey.fromStringECDSA(privateKeyString);
        PublicKey key = privateKey.getPublicKey();

        assertThat(key.toEvmAddress()).hasToString(expectedEvmAddress);
    }
}
