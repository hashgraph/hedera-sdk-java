package com.hedera.hashgraph.sdk.crypto;

import com.hedera.hashgraph.proto.SignaturePair;
import com.hedera.hashgraph.sdk.Internal;

import java.security.SecureRandom;

public abstract class PrivateKey<PubKey extends PublicKey> {
    /**
     * A {@link SecureRandom} instance that implementations can use by default.
     */
    // SecureRandom.getInstanceStrong() is horrible inside Docker (because it blocks forever
    // waiting for entropy) so we avoid it
    protected static final SecureRandom secureRandom = new SecureRandom();

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
    public abstract SignaturePair sign(byte[] message, int messageOffset, int messageLen);

    @Internal
    public SignaturePair sign(byte[] message) {
        return sign(message, 0, message.length);
    }
}
