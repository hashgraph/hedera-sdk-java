package com.hedera.hashgraph.sdk;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ECDSAPrivateKeyTest {
    @Test
    @DisplayName("private key generates successfully")
    void keyGenerates() {
        PrivateKey key = PrivateKey.generateECDSA();

        assertNotNull(key);
        assertNotNull(key.toBytes());
    }

    @Test
    @DisplayName("private key can be recovered from bytes")
    void keySerialization() {
        PrivateKey key1 = PrivateKey.generateECDSA();
        byte[] key1Bytes = key1.toBytes();
        PrivateKey key2 = PrivateKey.fromBytes(key1Bytes);
        byte[] key2Bytes = key2.toBytes();

        assertArrayEquals(key1Bytes, key2Bytes);
    }

    @Test
    @DisplayName("private key can be recovered from raw bytes")
    void keySerialization2() {
        PrivateKey key1 = PrivateKey.generateECDSA();
        byte[] key1Bytes = key1.toBytesRaw();
        PrivateKey key2 = PrivateKey.fromBytesECDSA(key1Bytes);
        byte[] key2Bytes = key2.toBytesRaw();
        // cannot use PrivateKey.fromBytes() to parse raw ECDSA bytes
        // because they're indistinguishable from ED25519 raw bytes

        assertArrayEquals(key1Bytes, key2Bytes);
    }

    @Test
    @DisplayName("private key can be recovered from DER bytes")
    void keySerialization3() {
        PrivateKey key1 = PrivateKey.generateECDSA();
        byte[] key1Bytes = key1.toBytesDER();
        PrivateKey key2 = PrivateKey.fromBytesDER(key1Bytes);
        byte[] key2Bytes = key2.toBytesDER();
        PrivateKey key3 = PrivateKey.fromBytes(key1Bytes);
        byte[] key3Bytes = key3.toBytesDER();

        assertArrayEquals(key1Bytes, key2Bytes);
        assertArrayEquals(key1Bytes, key3Bytes);
    }

    @Test
    @DisplayName("private key can be recovered from string")
    void keyStringification() {
        PrivateKey key1 = PrivateKey.generateECDSA();
        String key1String = key1.toString();
        PrivateKey key2 = PrivateKey.fromString(key1String);
        String key2String = key2.toString();

        assertEquals(key1String, key2String);
    }

    @Test
    @DisplayName("private key can be recovered from raw string")
    void keyStringification2() {
        PrivateKey key1 = PrivateKey.generateECDSA();
        String key1String = key1.toStringRaw();
        PrivateKey key2 = PrivateKey.fromStringECDSA(key1String);
        String key2String = key2.toStringRaw();
        // cannot use PrivateKey.fromString() to parse raw ECDSA string
        // because it's indistinguishable from ED25519 raw string

        assertEquals(key1String, key2String);
    }

    @Test
    @DisplayName("private key can be recovered from DER string")
    void keyStringification3() {
        PrivateKey key1 = PrivateKey.generateECDSA();
        String key1String = key1.toStringDER();
        PrivateKey key2 = PrivateKey.fromStringDER(key1String);
        String key2String = key2.toStringDER();
        PrivateKey key3 = PrivateKey.fromString(key1String);
        String key3String = key3.toStringDER();

        assertEquals(key1String, key2String);
        assertEquals(key1String, key3String);
    }

    // TODO: get fromPem working, and test it
}
