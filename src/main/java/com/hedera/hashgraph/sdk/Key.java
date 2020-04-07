package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.SignaturePair;

public abstract class Key {
    static Key fromProtobuf(com.hedera.hashgraph.sdk.proto.Key key) {
        switch (key.getKeyCase()) {
            case ED25519:
                return new PublicKey(key.getEd25519().toByteArray());

            case KEYLIST:
                return KeyList.fromProtobuf(key.getKeyList(), null);

            case THRESHOLDKEY:
                return KeyList.fromProtobuf(key.getThresholdKey().getKeys(), key.getThresholdKey().getThreshold());

            default:
                throw new IllegalStateException("Key#fromProtobuf: unhandled key case: " + key.getKeyCase());
        }
    }

    /**
     * Serialize this key as a protobuf object
     */
    abstract com.hedera.hashgraph.sdk.proto.Key toKeyProtobuf();

    /**
     * Serialize this key as a SignaturePair protobuf object
     */
    abstract SignaturePair toSignaturePairProtobuf(byte[] signature);
}
