package com.hedera.hashgraph.sdk.crypto;

import java.security.SecureRandom;

public abstract class PrivateKey {
    /**
     * A {@link SecureRandom} instance that implementations can use by default.
     */
    // SecureRandom.getInstanceStrong() is horrible inside Docker (because it blocks forever
    // waiting for entropy) so we avoid it
    protected static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Get the private key as DER encoded bytes.
     *
     * @return the encoded private key.
     */
    public abstract byte[] toBytes();
}
