package com.hedera.hashgraph.sdk.crypto;

/**
 * Builder for a list of keys which, if set on an account, must all sign transactions created by
 * that account.
 *
 * Equivalent to a threshold key with a threshold the same as the number of keys it contains.
 */
public class KeyList extends PublicKey {
    private com.hederahashgraph.api.proto.java.KeyList.Builder keyListBuilder =
        com.hederahashgraph.api.proto.java.KeyList.newBuilder();

    public KeyList() { }

    public KeyList addKey(PublicKey key) {
        keyListBuilder.addKeys(key.toKeyProto());
        return this;
    }

    @Override
    public com.hederahashgraph.api.proto.java.Key toKeyProto() {
        com.hederahashgraph.api.proto.java.Key.Builder key = com.hederahashgraph.api.proto.java.Key.newBuilder();
        key.setKeyList(keyListBuilder.build());
        return key.build();
    }
}
