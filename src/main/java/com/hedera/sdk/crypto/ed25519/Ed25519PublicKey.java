package com.hedera.sdk.crypto.ed25519;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.math.ec.rfc8032.Ed25519;

/**
 * An ed25519 public key.
 *
 * <p>Can be constructed from a byte array or obtained from a private key {@link
 * Ed25519PrivateKey#getPublicKey()}.
 */
public final class Ed25519PublicKey {
    private final Ed25519PublicKeyParameters publicKey;

    Ed25519PublicKey(Ed25519PublicKeyParameters publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Construct a known public key from its bytes.
     *
     * @throws AssertionError if {@code bytes.length != 32}
     */
    public static Ed25519PublicKey fromBytes(byte[] bytes) {
        return fromBytes(bytes, 0);
    }

    /**
     * Construct a known public key from a byte array and an offset into that array.
     *
     * @throws AssertionError if {@code offset >= bytes.length || bytes.length - offset != 32}
     */
    public static Ed25519PublicKey fromBytes(byte[] bytes, int offset) {
        assert offset < bytes.length;
        assert bytes.length - offset == Ed25519.PUBLIC_KEY_SIZE;
        return new Ed25519PublicKey(new Ed25519PublicKeyParameters(bytes, offset));
    }

    public byte[] toBytes() {
        return publicKey.getEncoded();
    }

    @Override
    public String toString() {
        SubjectPublicKeyInfo publicKeyInfo;

        try {
            publicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(publicKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ASN1OctetString.getInstance(publicKeyInfo).toString();
    }
}
