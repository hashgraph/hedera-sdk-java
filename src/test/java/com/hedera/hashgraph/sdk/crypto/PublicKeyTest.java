package com.hedera.hashgraph.sdk.crypto;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.Key;
import com.hedera.hashgraph.proto.KeyList;
import com.hedera.hashgraph.proto.KeyOrBuilder;
import com.hedera.hashgraph.proto.ThresholdKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class PublicKeyTest {
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
