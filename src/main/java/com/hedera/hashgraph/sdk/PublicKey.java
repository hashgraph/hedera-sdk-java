package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import org.bouncycastle.math.ec.rfc8032.Ed25519;

// TODO: Multiple algorithms
// TODO: #fromString
// TODO: #fromBytes

/**
 * A public key on the Hedera™ network.
 */
public final class PublicKey extends Key {
    private final byte[] keyData;

    PublicKey(byte[] keyData) {
        this.keyData = keyData;
    }

    /**
     * Verify a signature on a message with this public key.
     */
    public boolean verify(byte[] message, byte[] signature) {
        return Ed25519.verify(signature, 0, keyData, 0, message, 0, message.length);
    }

    @Override
    com.hedera.hashgraph.sdk.proto.Key toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setEd25519(ByteString.copyFrom(keyData))
            .build();
    }
}
