package com.hedera.sdk.crypto.ed25519;

import java.io.IOException;
import java.security.SecureRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.bouncycastle.util.encoders.Hex;

/**
 * An ed25519 private key.
 *
 * <p>To obtain an instance, see {@link #generate()} or {@link #fromBytes(byte[])}.
 */
public final class Ed25519PrivateKey {
    private final Ed25519PrivateKeyParameters privateKey;
    // computed from private key and memoized
    @Nullable private Ed25519PublicKey publicKey;

    private Ed25519PrivateKey(Ed25519PrivateKeyParameters privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * Recover a private key from its binary representation.
     *
     * @param privateKeyBytes the previously generated binary obtained from {@link #toBytes}
     * @return the restored private key
     * @throws AssertionError if {@code privateKeyBytes.length != 32}
     * @see #fromBytes(byte[], int)
     */
    @Nonnull
    public static Ed25519PrivateKey fromBytes(byte[] privateKeyBytes) {
        assert privateKeyBytes.length == Ed25519.SECRET_KEY_SIZE;
        return fromBytes(privateKeyBytes, 0);
    }

    /**
     * Recover a private key from its binary representation.
     *
     * @param privateKeyBytes the previously generated binary obtained from {@link #toBytes()}
     * @param offset the offset into {@code privateKeyBytes} at which to read the private key
     * @return the restored private key
     * @throws AssertionError if {@code offset >= privateKeyBytes.length || privateKeyBytes.length -
     *     offset != 32}
     */
    @Nonnull
    public static Ed25519PrivateKey fromBytes(byte[] privateKeyBytes, int offset) {
        assert offset < privateKeyBytes.length;
        assert privateKeyBytes.length - offset == Ed25519.SECRET_KEY_SIZE;

        var privateKeyParams = new Ed25519PrivateKeyParameters(privateKeyBytes, 0);
        return new Ed25519PrivateKey(privateKeyParams);
    }

    /**
     * Recover a private key from its text-encoded representation.
     *
     * @param privateKeyString the hex-encoded private key string
     * @return the restored private key
     * @throws org.bouncycastle.util.encoders.DecoderException if the hex string is invalid
     * @throws AssertionError if the hex string decodes to the wrong number of bytes
     */
    @Nonnull
    public static Ed25519PrivateKey fromString(String privateKeyString) {
        return fromBytes(Hex.decode(privateKeyString));
    }

    /** @return a new private key using {@link java.security.SecureRandom} */
    @Nonnull
    public static Ed25519PrivateKey generate() {
        return generate(new SecureRandom());
    }

    /** @return a new private key using the given {@link java.security.SecureRandom} */
    @Nonnull
    public static Ed25519PrivateKey generate(SecureRandom secureRandom) {
        return new Ed25519PrivateKey(new Ed25519PrivateKeyParameters(secureRandom));
    }

    /** @return the public key counterpart of this private key to share with the hashgraph */
    @Nonnull
    public Ed25519PublicKey getPublicKey() {
        if (publicKey == null) {
            publicKey = new Ed25519PublicKey(privateKey.generatePublicKey());
        }

        return publicKey;
    }

    /**
     * @return the encoded binary representation of this private key; can be restored later with
     *     {@link #fromBytes(byte[])}
     */
    @Nonnull
    public byte[] toBytes() {
        return privateKey.getEncoded();
    }

    @SuppressWarnings("Duplicates")
    @Override
    @Nonnull
    public String toString() {
        PrivateKeyInfo privateKeyInfo;
        try {
            privateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(privateKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // I'd love to dedup this with the code in `Ed25519PublicKey.toString()`
        // but there's no way to do that without creating an entirely public class
        byte[] encoded;

        try {
            encoded = privateKeyInfo.getEncoded("DER");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Hex.toHexString(encoded);
    }
}
