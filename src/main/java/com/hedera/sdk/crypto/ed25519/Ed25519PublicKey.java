package com.hedera.sdk.crypto.ed25519;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.math.ec.rfc8032.Ed25519;

import java.io.IOException;

/**
 * An ed25519 public key.
 *
 * Can be constructed from a byte array or generated with {@link Ed25519KeyPair}.
 */
public final class Ed25519PublicKey {
    private final Ed25519PublicKeyParameters publicKey;

    Ed25519PublicKey(Ed25519PublicKeyParameters publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Construct a known public key from its bytes.
     * @throws AssertionError if {@code bytes.length != 32}
     */
    public Ed25519PublicKey(byte[] bytes) {
        this(bytes, 0);
    }

    /**
     * Construct a known public key from a byte array and an offset into that array.
     * @throws AssertionError if {@code offset >= bytes.length || bytes.length - offset != 32}
     */
    public Ed25519PublicKey(byte[] bytes, int offset) {
        assert offset < bytes.length;
        assert bytes.length - offset == Ed25519.PUBLIC_KEY_SIZE;
        publicKey = new Ed25519PublicKeyParameters(bytes, offset);
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
