package com.hedera.hashgraph.sdk.crypto;

import com.hedera.hashgraph.sdk.Internal;

public abstract class PrivateKey<PubKey extends PublicKey> {

    /**
     * The public-key half of this private key.
     */
    public final PubKey publicKey;

    protected PrivateKey(PubKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Get the private key as DER encoded bytes.
     *
     * @return the encoded private key.
     */
    public abstract byte[] toBytes();

    @Internal
    public abstract byte[] sign(byte[] message, int messageOffset, int messageLen);

    @Internal
    public byte[] sign(byte[] message) {
        return sign(message, 0, message.length);
    }
}
