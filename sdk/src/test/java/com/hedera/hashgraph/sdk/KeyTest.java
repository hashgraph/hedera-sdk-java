package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.Key;
import com.hedera.hashgraph.sdk.proto.KeyList;
import com.hedera.hashgraph.sdk.proto.ThresholdKey;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeyTest {
    @Test
    @DisplayName("can sign and verify a message")
    void signatureVerified() {
        var message = "Hello, World".getBytes(UTF_8);
        var privateKey = PrivateKey.generate();
        var publicKey = privateKey.getPublicKey();
        var signature = privateKey.sign(message);

        assertEquals(signature.length, 64);
        assertTrue(publicKey.verify(message, signature));
    }

    @Test
    @DisplayName("can convert from protobuf ED25519 key to PublicKey")
    void fromProtoKeyEd25519() {
        var keyBytes = Hex.decode("0011223344556677889900112233445566778899001122334455667788990011");
        var protoKey = Key.newBuilder().setEd25519(ByteString.copyFrom(keyBytes)).build();

        var cut = PublicKey.fromProtobufKey(protoKey);

        assertEquals(cut.getClass(), PublicKey.class);
        assertArrayEquals(keyBytes, cut.toBytes());
    }

    @Test
    @DisplayName("can convert from protobuf key list to PublicKey")
    void fromProtoKeyKeyList() {
        // given
        var keyBytes = new byte[][]{
            Hex.decode("0011223344556677889900112233445566778899001122334455667788990011"),
            Hex.decode("aa11223344556677889900112233445566778899001122334455667788990011")
        };

        var protoKeyList = KeyList.newBuilder();

        for (byte[] kb : keyBytes) {
            protoKeyList.addKeys(Key.newBuilder().setEd25519(ByteString.copyFrom(kb)));
        }

        var protoKey = Key.newBuilder().setKeyList(protoKeyList).build();

        // when
        var cut = com.hedera.hashgraph.sdk.Key.fromProtobufKey(protoKey);

        // then
        assertEquals(cut.getClass(), com.hedera.hashgraph.sdk.KeyList.class);

        var keyList = (com.hedera.hashgraph.sdk.KeyList) cut;
        var actual = keyList.toProtobufKey().getKeyList();

        assertEquals(2, actual.getKeysCount());
        assertArrayEquals(keyBytes[0], actual.getKeys(0).getEd25519().toByteArray());
        assertArrayEquals(keyBytes[1], actual.getKeys(1).getEd25519().toByteArray());
    }

    @Test
    @DisplayName("can convert from protobuf threshold key to PublicKey")
    void fromProtoKeyThresholdKey() {
        // given
        var keyBytes = new byte[][]{
            Hex.decode("0011223344556677889900112233445566778899001122334455667788990011"),
            Hex.decode("aa11223344556677889900112233445566778899001122334455667788990011")
        };

        var protoKeyList = KeyList.newBuilder();

        for (byte[] kb : keyBytes) {
            protoKeyList.addKeys(Key.newBuilder().setEd25519(ByteString.copyFrom(kb)));
        }

        var protoThresholdKey = ThresholdKey.newBuilder().setThreshold(1).setKeys(protoKeyList);
        var protoKey = Key.newBuilder().setThresholdKey(protoThresholdKey).build();

        // when
        var cut = com.hedera.hashgraph.sdk.Key.fromProtobufKey(protoKey);

        // then
        assertEquals(cut.getClass(), com.hedera.hashgraph.sdk.KeyList.class);

        var thresholdKey = (com.hedera.hashgraph.sdk.KeyList) cut;
        var actual = thresholdKey.toProtobufKey().getThresholdKey();

        assertEquals(1, actual.getThreshold());
        assertEquals(2, actual.getKeys().getKeysCount());
        assertArrayEquals(keyBytes[0], actual.getKeys().getKeys(0).getEd25519().toByteArray());
        assertArrayEquals(keyBytes[1], actual.getKeys().getKeys(1).getEd25519().toByteArray());
    }

    @Test
    @DisplayName("Throws given unsupported key")
    void throwsUnsupportedKey() {
        byte[] keyBytes = {0, 1, 2};
        var protoKey = Key.newBuilder().setRSA3072(ByteString.copyFrom(keyBytes)).build();
        assertThrows(IllegalStateException.class, () -> com.hedera.hashgraph.sdk.Key.fromProtobufKey(protoKey));
    }

    @Test
    @DisplayName("Key equals")
    void keyEquals() {
        var key1 = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

        var key2 = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

        assertEquals(key1.toString(), key2.toString());
        assertEquals(key1.getPublicKey(), key2.getPublicKey());
        assertNotEquals(key1.getPublicKey(), "random string");
    }

    @Test
    @DisplayName("Key has hash")
    void keyHash() {
        var key = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

        assertDoesNotThrow(() -> key.hashCode());
    }

    @Test
    @DisplayName("KeyList methods")
    void keyListMethods() {
        var key1 = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

        var key2 = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e11");

        var key3 = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e12");

        var keyList = com.hedera.hashgraph.sdk.KeyList.withThreshold(1);
        keyList.add(key1);
        keyList.addAll(List.of(key2, key3));

        assertFalse(keyList.isEmpty());
        assertEquals(keyList.size(), 3);
        assertTrue(keyList.contains(key1));
        assertTrue(keyList.contains(key2));
        assertTrue(keyList.contains(key3));

        @Var var arr = keyList.toArray();
        assertEquals(arr[0], key1);
        assertEquals(arr[1], key2);
        assertEquals(arr[2], key3);

        arr = new com.hedera.hashgraph.sdk.Key[]{null, null, null};
        keyList.toArray(arr);
        assertEquals(arr[0], key1);
        assertEquals(arr[1], key2);
        assertEquals(arr[2], key3);

        keyList.remove(key2);
        assertEquals(keyList.size(), 2);

        keyList.clear();

        keyList.addAll(List.of(key1, key2, key3));
        assertEquals(keyList.size(), 3);

        keyList.retainAll(List.of(key2, key3));

        assertEquals(keyList.size(), 2);
        assertTrue(keyList.containsAll(List.of(key2, key3)));

        keyList.removeAll(List.of(key2, key3));
        assertTrue(keyList.isEmpty());
    }
}
