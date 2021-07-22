package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Ed25519PublicKeyTest {
    private static final String TEST_KEY_STR = "302a300506032b6570032100e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7";
    private static final String TEST_KEY_STR_RAW = "e0c8ec2758a5879ffac226a13c0c516b799e72e35141a0dd828f94d37988a4b7";

    @Test
    @DisplayName("private key can be recovered from bytes")
    void keyByteSerialization() {
        PublicKey key1 = PrivateKey.generate().getPublicKey();
        byte[] key1Bytes = key1.toBytes();
        PublicKey key2 = PublicKey.fromBytes(key1Bytes);
        byte[] key2Bytes = key2.toBytes();

        assertArrayEquals(key1Bytes, key2Bytes);
    }

    @Test
    @DisplayName("private key can be recovered from string")
    void keyStringSerialization() {
        PublicKey key1 = PrivateKey.generate().getPublicKey();
        String key1Str = key1.toString();
        PublicKey key2 = PublicKey.fromString(key1Str);
        String key2Str = key2.toString();
        PublicKey key3 = PublicKey.fromString(key1Str);
        String key3Str = key3.toString();

        assertEquals(key3.getClass(), PublicKey.class);
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
        PublicKey key = PublicKey.fromString(keyStr);
        assertNotNull(key);
        // the above are all the same key
        assertEquals(TEST_KEY_STR, key.toString());
        assertEquals(TEST_KEY_STR, key.toStringDER());
        assertEquals(TEST_KEY_STR_RAW, key.toStringRaw());
    }

    @Test
    @DisplayName("public key can be encoded to a string")
    void keyToString() {
        PublicKey key = PublicKey.fromString(TEST_KEY_STR);

        assertNotNull(key);
        assertEquals(TEST_KEY_STR, key.toString());
    }
}
