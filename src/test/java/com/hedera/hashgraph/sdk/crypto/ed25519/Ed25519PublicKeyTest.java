package com.hedera.hashgraph.sdk.crypto.ed25519;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.Key;
import com.hedera.hashgraph.proto.KeyList;
import com.hedera.hashgraph.proto.KeyListOrBuilder;
import com.hedera.hashgraph.proto.KeyOrBuilder;
import com.hedera.hashgraph.proto.ThresholdKey;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

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

    @Test
    @DisplayName("can convert from protobuf ED25519 key to PublicKey")
    void fromProtoKeyEd25519() {
        final byte[] keyBytes = Hex.decode("0011223344556677889900112233445566778899001122334455667788990011");
        final Key protoKey = Key.newBuilder().setEd25519(ByteString.copyFrom(keyBytes)).build();

        final PublicKey cut = PublicKey.fromProtoKey(protoKey);

        assertEquals(cut.getClass(), Ed25519PublicKey.class);
        assertArrayEquals(keyBytes, ((Ed25519PublicKey)cut).toBytes());
    }

    @Test
    @DisplayName("can convert from protobuf key list to PublicKey")
    void fromProtoKeyKeyList() {
        // given
        final byte[][] keyBytes = new byte[][] {
            Hex.decode("0011223344556677889900112233445566778899001122334455667788990011"),
            Hex.decode("aa11223344556677889900112233445566778899001122334455667788990011")
        };
        final KeyList.Builder protoKeyList = KeyList.newBuilder();
        Arrays.stream(keyBytes).forEach(kb -> {
            protoKeyList.addKeys(Key.newBuilder().setEd25519(ByteString.copyFrom(kb)));
        });
        final KeyOrBuilder protoKey = Key.newBuilder().setKeyList(protoKeyList).build();

        // when
        final PublicKey cut = PublicKey.fromProtoKey(protoKey);

        // then
        assertEquals(cut.getClass(), com.hedera.hashgraph.sdk.crypto.KeyList.class);
        final com.hedera.hashgraph.sdk.crypto.KeyList keyList = (com.hedera.hashgraph.sdk.crypto.KeyList)cut;
        final KeyList actual = keyList.toKeyProto().getKeyList();
        assertEquals(2, actual.getKeysCount());
        assertArrayEquals(keyBytes[0], actual.getKeys(0).getEd25519().toByteArray());
        assertArrayEquals(keyBytes[1], actual.getKeys(1).getEd25519().toByteArray());
    }

    @Test
    @DisplayName("can convert from protobuf threshold key to PublicKey")
    void fromProtoKeyThresholdKey() {
        // given
        final byte[][] keyBytes = new byte[][] {
            Hex.decode("0011223344556677889900112233445566778899001122334455667788990011"),
            Hex.decode("aa11223344556677889900112233445566778899001122334455667788990011")
        };
        final KeyList.Builder protoKeyList = KeyList.newBuilder();
        Arrays.stream(keyBytes).forEach(kb -> {
            protoKeyList.addKeys(Key.newBuilder().setEd25519(ByteString.copyFrom(kb)));
        });
        final ThresholdKey.Builder protoThresholdKey = ThresholdKey.newBuilder().setThreshold(1).setKeys(protoKeyList);
        final KeyOrBuilder protoKey = Key.newBuilder().setThresholdKey(protoThresholdKey).build();

        // when
        final PublicKey cut = PublicKey.fromProtoKey(protoKey);

        // then
        assertEquals(cut.getClass(), com.hedera.hashgraph.sdk.crypto.ThresholdKey.class);
        final com.hedera.hashgraph.sdk.crypto.ThresholdKey thresholdKey =
            (com.hedera.hashgraph.sdk.crypto.ThresholdKey)cut;
        final ThresholdKey actual = thresholdKey.toKeyProto().getThresholdKey();
        assertEquals(1, actual.getThreshold());
        assertEquals(2, actual.getKeys().getKeysCount());
        assertArrayEquals(keyBytes[0], actual.getKeys().getKeys(0).getEd25519().toByteArray());
        assertArrayEquals(keyBytes[1], actual.getKeys().getKeys(1).getEd25519().toByteArray());
    }
}
