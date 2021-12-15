package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.math.ec.rfc8032.Ed25519;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;

public class PublicKeyECDSA extends PublicKey {
    // Compressed 33 byte form
    private byte[] keyData;

    PublicKeyECDSA(byte[] keyData) {
        this.keyData = keyData;
    }

    static PublicKeyED25519 fromBytesInternal(byte[] publicKey) {
        // TODO
        if (publicKey.length == Ed25519.PUBLIC_KEY_SIZE) {
            // If this is a 32 byte string, assume an Ed25519 public key
            return new PublicKeyED25519(publicKey);
        }

        // Assume a DER-encoded private key descriptor
        return fromSubjectKeyInfoED25519(SubjectPublicKeyInfo.getInstance(publicKey));
    }

    static PublicKeyED25519 fromSubjectKeyInfoInternal(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        // TODO
        return new PublicKeyED25519(subjectPublicKeyInfo.getPublicKeyData().getBytes());
    }

    @Override
    public boolean verify(byte[] message, byte[] signature) {
        // TODO
        return Ed25519.verify(signature, 0, keyData, 0, message, 0, message.length);
    }

    @Override
    com.hedera.hashgraph.sdk.proto.Key toProtobufKey() {
        // TODO
        return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setEd25519(ByteString.copyFrom(keyData))
            .build();
    }

    @Override
    SignaturePair toSignaturePairProtobuf(byte[] signature) {
        // TODO
        return SignaturePair.newBuilder()
            .setPubKeyPrefix(ByteString.copyFrom(keyData))
            .setEd25519(ByteString.copyFrom(signature))
            .build();
    }

    @Override
    public byte[] toBytesDER() {
        // TODO
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
    public byte[] toBytes() {
        return toBytesDER();
    }

    @Override
    public byte[] toBytesRaw() {
        // TODO
        return keyData;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        // TODO
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PublicKeyED25519 publicKey = (PublicKeyED25519) o;
        return Arrays.equals(keyData, publicKey.keyData);
    }

    @Override
    public int hashCode() {
        // TODO
        return Arrays.hashCode(keyData);
    }
}
