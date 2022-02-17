package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

        Assertions.assertTrue(key.getPublicKey().verifyTransaction(transaction));
    }

    @Test
    @DisplayName("public key can be recovered from bytes")
    void keyByteSerialization() {
        PublicKey key1 = PrivateKey.generateED25519().getPublicKey();
        byte[] key1Bytes = key1.toBytes();
        PublicKey key2 = PublicKey.fromBytes(key1Bytes);
        byte[] key2Bytes = key2.toBytes();

        assertArrayEquals(key1Bytes, key2Bytes);
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

        assertArrayEquals(key1Bytes, key2Bytes);
        assertArrayEquals(key1Bytes, key3Bytes);
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

        assertArrayEquals(key1Bytes, key2Bytes);
        assertArrayEquals(key1Bytes, key3Bytes);
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

        assertEquals(PublicKeyED25519.class, key3.getClass());
        assertEquals(key1Str, key2Str);
        assertEquals(key1Str, key3Str);
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

        assertEquals(PublicKeyED25519.class, key3.getClass());
        assertEquals(key1Str, key2Str);
        assertEquals(key1Str, key3Str);
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

        assertEquals(PublicKeyED25519.class, key3.getClass());
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

    @Test
    @DisplayName("public key is is ECDSA")
    void keyIsECDSA() {
        PublicKey key = PrivateKey.generateECDSA().getPublicKey();

        assertTrue(key.isECDSA());
    }

    @Test
    @DisplayName("public key is is not Ed25519")
    void keyIsNotEd25519() {
        PublicKey key = PrivateKey.generateECDSA().getPublicKey();

        assertFalse(key.isED25519());
    }
}
