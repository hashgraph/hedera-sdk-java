package com.hedera.hashgraph.sdk.crypto.ed25519;

import com.hedera.hashgraph.sdk.crypto.PublicKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Ed25519PublicKeyTest {

    private static final String testKeyStr = "302a300506032b6570032100e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7";


    @Test
    @DisplayName("private key can be recovered from bytes")
    void keyByteSerialization() {
        final Ed25519PublicKey key1 = Ed25519PrivateKey.generate().getPublicKey();
        final byte[] key1Bytes = key1.toBytes();
        final Ed25519PublicKey key2 = Ed25519PublicKey.fromBytes(key1Bytes);
        final byte[] key2Bytes = key2.toBytes();

        assertArrayEquals(key1Bytes, key2Bytes);
    }

    @Test
    @DisplayName("private key can be recovered from string")
    void keyStringSerialization() {
        final Ed25519PublicKey key1 = Ed25519PrivateKey.generate().getPublicKey();
        final String key1Str = key1.toString();
        final Ed25519PublicKey key2 = Ed25519PublicKey.fromString(key1Str);
        final String key2Str = key2.toString();
        final PublicKey key3 = PublicKey.fromString(key1Str);
        final String key3Str = key3.toString();

        assertEquals(key3.getClass(), Ed25519PublicKey.class);
        assertEquals(key1Str, key2Str);
        assertEquals(key1Str, key3Str);
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
        final Ed25519PublicKey key = Ed25519PublicKey.fromString(keyStr);
        assertNotNull(key);
        // the above are all the same key
        assertEquals(testKeyStr, key.toString());
    }

    @Test
    @DisplayName("public key can be encoded to a string")
    void keyToString() {
        final Ed25519PublicKey key = Ed25519PublicKey.fromString(testKeyStr);

        assertNotNull(key);
        assertEquals(testKeyStr, key.toString());
    }
}
