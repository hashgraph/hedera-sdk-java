package com.hedera.sdk.crypto.ed25519;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;

import java.io.IOException;

/**
 * An ed25519 private key.
 * <p>
 * To obtain an instance, see {@link Ed25519KeyPair}.
 */
public final class Ed25519PrivateKey {
    private final Ed25519PrivateKeyParameters privateKey;

    Ed25519PrivateKey(Ed25519PrivateKeyParameters privateKey) {
        this.privateKey = privateKey;
    }

    public byte[] toBytes() {
        return privateKey.getEncoded();
    }

    @Override
    public String toString() {
        PrivateKeyInfo privateKeyInfo;
        try {
            privateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(privateKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ASN1OctetString.getInstance(privateKeyInfo).toString();
    }
}
