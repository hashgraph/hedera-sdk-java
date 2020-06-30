package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.Key;
import com.hedera.hashgraph.sdk.proto.KeyList;
import com.hedera.hashgraph.sdk.proto.ThresholdKey;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

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

        var cut = PublicKey.fromProtobuf(protoKey);

        assertEquals(cut.getClass(), PublicKey.class);
        assertArrayEquals(keyBytes, ((PublicKey) cut).toBytes());
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
        var cut = com.hedera.hashgraph.sdk.Key.fromProtobuf(protoKey);

        // then
        assertEquals(cut.getClass(), com.hedera.hashgraph.sdk.KeyList.class);

        var keyList = (com.hedera.hashgraph.sdk.KeyList) cut;
        var actual = keyList.toKeyProtobuf().getKeyList();

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
        var cut = com.hedera.hashgraph.sdk.Key.fromProtobuf(protoKey);

        // then
        assertEquals(cut.getClass(), com.hedera.hashgraph.sdk.KeyList.class);

        var thresholdKey = (com.hedera.hashgraph.sdk.KeyList) cut;
        var actual = thresholdKey.toKeyProtobuf().getThresholdKey();

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
        assertThrows(IllegalStateException.class, () -> com.hedera.hashgraph.sdk.Key.fromProtobuf(protoKey));
    }

    @Test
    @DisplayName("PublicKey equals")
    void keyEquals() {
        var key1 = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

        var key2 = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

        assertEquals(key1, key2);
        assertEquals(key1, key1);
        assertEquals(key2, key2);
        assertEquals(key1.getPublicKey(), key2.getPublicKey());
        assertEquals(key1.getPublicKey(), key1.getPublicKey());
        assertEquals(key2.getPublicKey(), key2.getPublicKey());
        assertNotEquals(key1.getPublicKey(), "random string");
    }

    @Test
    @DisplayName("PublicKey equals")
    void keyHash() {
        var key = PrivateKey.fromString(
            "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

        assertDoesNotThrow(() -> key.hashCode());
    }
}
