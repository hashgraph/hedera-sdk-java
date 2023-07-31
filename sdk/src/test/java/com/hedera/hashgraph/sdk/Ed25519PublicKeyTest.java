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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class Ed25519PublicKeyTest {
    private static final String TEST_KEY_STR = "302a300506032b6570032100e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7";
    private static final String TEST_KEY_STR_RAW = "e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7";

    @Test
    void verifyTransaction() {
        var transaction = new TransferTransaction()
                .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
                .setTransactionId(TransactionId.generate(new AccountId(4)))
                .freeze();

        var key = PrivateKey.fromStringED25519("8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048");
        key.signTransaction(transaction);

        assertThat(key.getPublicKey().verifyTransaction(transaction)).isTrue();
    }

    @Test
    @DisplayName("public key can be recovered from bytes")
    void keyByteSerialization() {
        PublicKey key1 = PrivateKey.generateED25519().getPublicKey();
        byte[] key1Bytes = key1.toBytes();
        PublicKey key2 = PublicKey.fromBytes(key1Bytes);
        byte[] key2Bytes = key2.toBytes();

        assertThat(key2Bytes).containsExactly(key1Bytes);
    }

    @Test
    @DisplayName("public key can be recovered from raw bytes")
    void keyByteSerialization2() {
        PublicKey key1 = PrivateKey.generateED25519().getPublicKey();
        byte[] key1Bytes = key1.toBytesRaw();
        PublicKey key2 = PublicKey.fromBytesED25519(key1Bytes);
        byte[] key2Bytes = key2.toBytesRaw();
        PublicKey key3 = PublicKey.fromBytes(key1Bytes);
        byte[] key3Bytes = key3.toBytesRaw();

        assertThat(key2Bytes).containsExactly(key1Bytes);
        assertThat(key3Bytes).containsExactly(key1Bytes);
    }

    @Test
    @DisplayName("public key can be recovered from DER bytes")
    void keyByteSerialization3() {
        PublicKey key1 = PrivateKey.generateED25519().getPublicKey();
        byte[] key1Bytes = key1.toBytesDER();
        PublicKey key2 = PublicKey.fromBytesDER(key1Bytes);
        byte[] key2Bytes = key2.toBytesDER();
        PublicKey key3 = PublicKey.fromBytes(key1Bytes);
        byte[] key3Bytes = key3.toBytesDER();

        assertThat(key2Bytes).containsExactly(key1Bytes);
        assertThat(key3Bytes).containsExactly(key1Bytes);
    }

    @Test
    @DisplayName("public key can be recovered from string")
    void keyStringSerialization() {
        PublicKey key1 = PrivateKey.generateED25519().getPublicKey();
        String key1Str = key1.toString();
        PublicKey key2 = PublicKey.fromString(key1Str);
        String key2Str = key2.toString();
        PublicKey key3 = PublicKey.fromString(key1Str);
        String key3Str = key3.toString();

        assertThat(key3.getClass()).isEqualTo(PublicKeyED25519.class);
        assertThat(key2Str).isEqualTo(key1Str);
        assertThat(key3Str).isEqualTo(key1Str);
    }

    @Test
    @DisplayName("public key can be recovered from raw string")
    void keyStringSerialization2() {
        PublicKey key1 = PrivateKey.generateED25519().getPublicKey();
        String key1Str = key1.toStringRaw();
        PublicKey key2 = PublicKey.fromStringED25519(key1Str);
        String key2Str = key2.toStringRaw();
        PublicKey key3 = PublicKey.fromString(key1Str);
        String key3Str = key3.toStringRaw();

        assertThat(key3.getClass()).isEqualTo(PublicKeyED25519.class);
        assertThat(key2Str).isEqualTo(key1Str);
        assertThat(key3Str).isEqualTo(key1Str);
    }

    @Test
    @DisplayName("public key can be recovered from DER string")
    void keyStringSerialization3() {
        PublicKey key1 = PrivateKey.generateED25519().getPublicKey();
        String key1Str = key1.toStringDER();
        PublicKey key2 = PublicKey.fromStringDER(key1Str);
        String key2Str = key2.toStringDER();
        PublicKey key3 = PublicKey.fromString(key1Str);
        String key3Str = key3.toStringDER();

        assertThat(key3.getClass()).isEqualTo(PublicKeyED25519.class);
        assertThat(key2Str).isEqualTo(key1Str);
        assertThat(key3Str).isEqualTo(key1Str);
    }

    @ParameterizedTest
    @DisplayName("public key can be recovered from external string")
    @ValueSource(strings = {
        // ASN1 encoded hex
        "302a300506032b6570032100e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7",
        // raw hex
        "e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7",
    })
    void externalKeyDeserialize(String keyStr) {
        PublicKey key = PublicKey.fromString(keyStr);
        assertThat(key).isNotNull();
        // the above are all the same key
        assertThat(key.toString()).isEqualTo(TEST_KEY_STR);
        assertThat(key.toStringDER()).isEqualTo(TEST_KEY_STR);
        assertThat(key.toStringRaw()).isEqualTo(TEST_KEY_STR_RAW);
    }

    @Test
    @DisplayName("public key can be encoded to a string")
    void keyToString() {
        PublicKey key = PublicKey.fromString(TEST_KEY_STR);

        assertThat(key).isNotNull();
        assertThat(key.toString()).isEqualTo(TEST_KEY_STR);
    }

    @Test
    @DisplayName("public key is is ED25519")
    void keyIsECDSA() {
        PublicKey key = PrivateKey.generateED25519().getPublicKey();

        assertThat(key.isED25519()).isTrue();
    }

    @Test
    @DisplayName("public key is is not ECDSA")
    void keyIsNotEd25519() {
        PublicKey key = PrivateKey.generateED25519().getPublicKey();

        assertThat(key.isECDSA()).isFalse();
    }

    @Test
    @DisplayName("DER import test vectors")
    void DERImportTestVectors() {
        // https://github.com/hashgraph/hedera-sdk-reference/issues/93#issue-1665972122
        var PUBLIC_KEY_DER1 = "302a300506032b65700321008ccd31b53d1835b467aac795dab19b274dd3b37e3daf12fcec6bc02bac87b53d";
        var PUBLIC_KEY1 = "8ccd31b53d1835b467aac795dab19b274dd3b37e3daf12fcec6bc02bac87b53d";

        var ed25519PublicKey1 = PublicKey.fromStringDER(PUBLIC_KEY_DER1);
        assertThat(ed25519PublicKey1.toStringRaw()).isEqualTo(PUBLIC_KEY1);
    }
}
