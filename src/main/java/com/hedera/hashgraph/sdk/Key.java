package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.SignaturePair;

public abstract class Key {
    /** Serialize this key as a protobuf object */
    abstract com.hedera.hashgraph.sdk.proto.Key toKeyProtobuf();

    /** Serialize this key as a SignaturePair protobuf object */
    abstract SignaturePair toSignaturePairProtobuf(byte[] signature);
}
