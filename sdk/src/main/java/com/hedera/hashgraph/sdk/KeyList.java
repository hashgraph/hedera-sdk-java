package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import com.hedera.hashgraph.sdk.proto.ThresholdKey;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class KeyList extends Key {
    @Nullable
    public final Integer threshold;

    public final List<Key> keys = new ArrayList<>();

    public KeyList() {
        this.threshold = null;
    }

    private KeyList(int threshold) {
        this.threshold = threshold;
    }

    public static KeyList withThreshold(int threshold) {
        return new KeyList(threshold);
    }

    static KeyList fromProtobuf(com.hedera.hashgraph.sdk.proto.KeyList keyList, @Nullable Integer threshold) {
        var keys = new Key[keyList.getKeysCount()];
        for (var i = 0; i < keys.length; ++i) {
            keys[i] = Key.fromProtobuf(keyList.getKeys(i));
        }

        return (threshold != null ? new KeyList(threshold) : new KeyList()).addAll(keys);
    }

    public KeyList add(Key key) {
        keys.add(key);
        return this;
    }

    public KeyList addAll(Key... keys) {
        Collections.addAll(this.keys, keys);
        return this;
    }

    @Override
    com.hedera.hashgraph.sdk.proto.Key toKeyProtobuf() {
        var protoKeyList = com.hedera.hashgraph.sdk.proto.KeyList.newBuilder();
        for (var key : keys) {
            protoKeyList.addKeys(key.toKeyProtobuf());
        }

        if (threshold != null) {
            return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
                .setThresholdKey(ThresholdKey.newBuilder()
                    .setThreshold(threshold)
                    .setKeys(protoKeyList))
                .build();
        }

        return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setKeyList(protoKeyList)
            .build();
    }

    @Override
    SignaturePair toSignaturePairProtobuf(byte[] signature) {
        throw new IllegalStateException("KeyList cannot be used with Transaction#signWith");
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("threshold", threshold)
            .add("keys", keys)
            .toString();
    }
}
