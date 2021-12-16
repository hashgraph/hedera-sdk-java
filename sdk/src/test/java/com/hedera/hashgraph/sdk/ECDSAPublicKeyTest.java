package com.hedera.hashgraph.sdk;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ECDSAPublicKeyTest {
    @Test
    @DisplayName("public key can be recovered from bytes")
    void keyByteSerialization() {
        PublicKey key1 = PrivateKey.generateECDSA().getPublicKey();
        byte[] key1Bytes = key1.toBytes();
        PublicKey key2 = PublicKey.fromBytes(key1Bytes);
        byte[] key2Bytes = key2.toBytes();

        assertArrayEquals(key1Bytes, key2Bytes);
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

        assertArrayEquals(key1Bytes, key2Bytes);
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

        assertArrayEquals(key1Bytes, key2Bytes);
        assertArrayEquals(key1Bytes, key3Bytes);
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

        assertEquals(PublicKeyECDSA.class, key3.getClass());
        assertEquals(key1Str, key2Str);
        assertEquals(key1Str, key3Str);
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

        assertEquals(PublicKeyECDSA.class, key3.getClass());
        assertEquals(key1Str, key2Str);
        assertEquals(key1Str, key3Str);
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

        assertEquals(PublicKeyECDSA.class, key3.getClass());
        assertEquals(key1Str, key2Str);
        assertEquals(key1Str, key3Str);
    }
}
