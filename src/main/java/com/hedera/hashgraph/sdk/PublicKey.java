package com.hedera.hashgraph.sdk;

import org.bouncycastle.math.ec.rfc8032.Ed25519;

// TODO: Multiple algorithms
// TODO: #fromString
// TODO: #fromBytes

/**
 * A public key on the Hedera™ network.
 */
public class PublicKey {
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
}
