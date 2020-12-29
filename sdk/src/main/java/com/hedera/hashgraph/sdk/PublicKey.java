package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.util.Arrays;

/**
 * A public key on the Hedera™ network.
 */
public final class PublicKey extends Key {
    private final byte[] keyData;

    PublicKey(byte[] keyData) {
        this.keyData = keyData;
    }

    public static PublicKey fromBytes(byte[] publicKey) {
        if (publicKey.length == Ed25519.PUBLIC_KEY_SIZE) {
            // If this is a 32 byte string, assume an Ed25519 public key
            return new PublicKey(publicKey);
        }

        // Assume a DER-encoded private key descriptor
        return PublicKey.fromSubjectKeyInfo(SubjectPublicKeyInfo.getInstance(publicKey));
    }

    public static PublicKey fromString(String publicKey) {
        return PublicKey.fromBytes(Hex.decode(publicKey));
    }

    private static PublicKey fromSubjectKeyInfo(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        return new PublicKey(subjectPublicKeyInfo.getPublicKeyData().getBytes());
    }

    /**
     * Verify a signature on a message with this public key.
     *
     * @param message   The array of bytes representing the message
     * @param signature The array of bytes representing the signature
     * @return boolean
     */
    public boolean verify(byte[] message, byte[] signature) {
        return Ed25519.verify(signature, 0, keyData, 0, message, 0, message.length);
    }

    public boolean verifyTransaction(Transaction<?> transaction) {
        if (!transaction.isFrozen()) {
            transaction.freeze();
        }

        for (var signedTransaction : transaction.signedTransactions) {
            for (var sigPair : signedTransaction.getSigMap().getSigPairList()) {
                if (
                    sigPair.getPubKeyPrefix().equals(ByteString.copyFrom(toBytes())) &&
                        !verify(signedTransaction.getBodyBytes().toByteArray(), sigPair.getECDSA384().toByteArray())
                ) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    com.hedera.hashgraph.sdk.proto.Key toProtobufKey() {
        return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setEd25519(ByteString.copyFrom(keyData))
            .build();
    }

    /**
     * Serialize this key as a SignaturePair protobuf object
     */
    SignaturePair toSignaturePairProtobuf(byte[] signature) {
        return SignaturePair.newBuilder()
            .setPubKeyPrefix(ByteString.copyFrom(keyData))
            .setEd25519(ByteString.copyFrom(signature))
            .build();
    }

    @Override
    public byte[] toBytes() {
        return keyData;
    }

    private byte[] toDER() {
        try {
            return new SubjectPublicKeyInfo(
                new AlgorithmIdentifier(ID_ED25519),
                keyData
            ).getEncoded("DER");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return Hex.toHexString(toDER());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PublicKey publicKey = (PublicKey) o;
        return Arrays.equals(keyData, publicKey.keyData);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(keyData);
    }
}
