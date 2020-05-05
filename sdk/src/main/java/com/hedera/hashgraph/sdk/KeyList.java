package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import com.hedera.hashgraph.sdk.proto.ThresholdKey;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A list of keys that are required to sign in unison, with an optional threshold controlling how many keys of
 * the list are required.
 */
public final class KeyList extends Key implements Collection<Key> {
    @Nullable
    public final Integer threshold;

    private final List<Key> keys = new ArrayList<>();

    /**
     * Create a new key list where all keys that are added will be required to sign.
     */
    public KeyList() {
        this.threshold = null;
    }

    private KeyList(int threshold) {
        this.threshold = threshold;
    }

    /**
     * Create a new key list where at least {@code threshold} keys must sign.
     *
     * @param threshold the minimum number of keys that must sign
     */
    public static KeyList withThreshold(int threshold) {
        return new KeyList(threshold);
    }

    static KeyList fromProtobuf(com.hedera.hashgraph.sdk.proto.KeyList keyList, @Nullable Integer threshold) {
        var keys = (threshold != null ? new KeyList(threshold) : new KeyList());
        for (var i = 0; i < keyList.getKeysCount(); ++i) {
            keys.add(Key.fromProtobuf(keyList.getKeys(i)));
        }

        return keys;
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return keys.contains(o);
    }

    @Override
    public Iterator<Key> iterator() {
        return keys.iterator();
    }

    @Override
    public Object[] toArray() {
        return keys.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        // noinspection unchecked,SuspiciousToArrayCall
        return (T[]) keys.toArray((Key[])ts);
    }

    @Override
    public boolean add(Key key) {
        return keys.add(key);
    }

    @Override
    public boolean remove(Object o) {
        return keys.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return keys.containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends Key> collection) {
        return keys.addAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return keys.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return keys.retainAll(collection);
    }

    @Override
    public void clear() {
        keys.clear();
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
