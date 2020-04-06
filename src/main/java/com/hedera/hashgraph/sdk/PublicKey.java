package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import org.bouncycastle.math.ec.rfc8032.Ed25519;

import java.util.Arrays;

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
    com.hedera.hashgraph.sdk.proto.Key toKeyProtobuf() {
        return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setEd25519(ByteString.copyFrom(keyData))
            .build();
    }

    @Override
    SignaturePair toSignaturePairProtobuf(byte[] signature) {
        return SignaturePair.newBuilder()
            .setPubKeyPrefix(ByteString.copyFrom(keyData))
            .setEd25519(ByteString.copyFrom(signature))
            .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PublicKey publicKey = (PublicKey) o;
        return Arrays.equals(keyData, publicKey.keyData);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(keyData);
    }
}
