package com.hedera.sdk.crypto.ed25519;

import com.hedera.sdk.crypto.KeyPair;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.math.ec.rfc8032.Ed25519;

import java.security.SecureRandom;

public final class Ed25519KeyPair extends KeyPair {
    public final Ed25519PrivateKey privateKey;
    public final Ed25519PublicKey publicKey;

    private Ed25519KeyPair(Ed25519PrivateKey privateKey, Ed25519PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    private Ed25519KeyPair(Ed25519PrivateKeyParameters privateKeyParams) {
        var publicKeyParams = privateKeyParams.generatePublicKey();

        privateKey = new Ed25519PrivateKey(privateKeyParams);
        publicKey = new Ed25519PublicKey(publicKeyParams);
    }

    /**
     * Recover a keypair from the bytes of the private key.
     *
     * @param privateKeyBytes the previously generated private key obtained from {@link Ed25519PrivateKey#toBytes}
     * @return a keypair restored from the given private key bytes
     * @throws AssertionError if {@code privateKeyBytes.length != 32}
     */
    public static Ed25519KeyPair fromBytes(byte[] privateKeyBytes) {
        assert privateKeyBytes.length == Ed25519.PUBLIC_KEY_SIZE;
        return fromBytes(privateKeyBytes, 0);
    }

    /**
     * Recover a keypair from the bytes of the private key.
     *
     * @param privateKeyBytes the previously generated private key obtained from {@link #privateKey}
     * @param offset          the offset into {@code privateKeyBytes} at which to read the private key
     * @return a keypair restored from the given section of the private key bytes
     * @throws AssertionError if {@code offset >= privateKeyBytes.length || privateKeyBytes.length - offset != 32}
     */
    public static Ed25519KeyPair fromBytes(byte[] privateKeyBytes, int offset) {
        assert offset < privateKeyBytes.length;
        assert privateKeyBytes.length - offset == Ed25519.SECRET_KEY_SIZE;

        var privateKeyParams = new Ed25519PrivateKeyParameters(privateKeyBytes, 0);
        return new Ed25519KeyPair(privateKeyParams);
    }

    /**
     * @return a new random keypair using {@link java.security.SecureRandom}
     */
    public static Ed25519KeyPair generate() {
        return generate(new SecureRandom());
    }

    /**
     * @return a new random keypair using the given {@link java.security.SecureRandom}
     */
    public static Ed25519KeyPair generate(SecureRandom secureRandom) {
        return new Ed25519KeyPair(new Ed25519PrivateKeyParameters(secureRandom));
    }
}
