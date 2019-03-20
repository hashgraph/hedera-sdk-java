package com.hedera.sdk.crypto.ed25519;

import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.math.ec.rfc8032.Ed25519;

public final class Ed25519Signature {
    private final byte[] sigBytes;

    private Ed25519Signature(byte[] sigBytes) {
        this.sigBytes = sigBytes;
    }

    public static Ed25519Signature fromBytes(byte[] signature) {
        assert signature.length == Ed25519.SIGNATURE_SIZE;
        return new Ed25519Signature(signature);
    }

    public static Ed25519Signature forMessage(Ed25519PrivateKey privateKey, byte[] message) {
        var signer = new Ed25519Signer();
        signer.init(true, privateKey.privKeyParams);
        signer.update(message, 0, message.length);
        var signature = signer.generateSignature();

        return new Ed25519Signature(signature);
    }

    public boolean verify(Ed25519PublicKey publicKey, byte[] message) {
        var signer = new Ed25519Signer();
        signer.init(false, publicKey.pubKeyParams);
        signer.update(message, 0, message.length);
        return signer.verifySignature(sigBytes);
    }
}
