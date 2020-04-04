package com.hedera.hashgraph.sdk;

public abstract class Key {
    /** Serialize this key as a protobuf object */
    abstract com.hedera.hashgraph.sdk.proto.Key toProtobuf();
}
