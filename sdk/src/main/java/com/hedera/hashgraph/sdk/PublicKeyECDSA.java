package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

public class PublicKeyECDSA extends PublicKey {
    // Compressed 33 byte form
    private byte[] keyData;

    PublicKeyECDSA(byte[] keyData) {
        this.keyData = keyData;
    }

    static PublicKeyECDSA fromBytesInternal(byte[] publicKey) {
        if (publicKey.length == 33) {
            // compressed 33 byte raw form
            return new PublicKeyECDSA(publicKey);
        } else if (publicKey.length == 65) {
            // compress the 65 byte form
            return new PublicKeyECDSA(
                Key.ECDSA_SECP256K1_CURVE.getCurve().decodePoint(publicKey).getEncoded(true)
            );
        }

        // Assume a DER-encoded public key descriptor
        return fromSubjectKeyInfoInternal(SubjectPublicKeyInfo.getInstance(publicKey));
    }

    static PublicKeyECDSA fromSubjectKeyInfoInternal(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        return new PublicKeyECDSA(subjectPublicKeyInfo.getPublicKeyData().getBytes());
    }

    @Override
    public boolean verify(byte[] message, byte[] signature) {
        var hash = Crypto.calcKeccak256(message);

        ECDSASigner signer = new ECDSASigner();
        signer.init(false, new ECPublicKeyParameters(
            Key.ECDSA_SECP256K1_CURVE.getCurve().decodePoint(keyData),
            Key.ECDSA_SECP256K1_DOMAIN
        ));

        BigInteger r = new BigInteger(1, Arrays.copyOf(signature, 32));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(signature, 32, 64));

        return signer.verifySignature(hash, r, s);
    }

    @Override
    com.hedera.hashgraph.sdk.proto.Key toProtobufKey() {
        return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setECDSASecp256K1(ByteString.copyFrom(keyData))
            .build();
    }

    @Override
    SignaturePair toSignaturePairProtobuf(byte[] signature) {
        return SignaturePair.newBuilder()
            .setPubKeyPrefix(ByteString.copyFrom(keyData))
            .setECDSASecp256K1(ByteString.copyFrom(signature))
            .build();
    }

    @Override
    public byte[] toBytesDER() {
        try {
            return new SubjectPublicKeyInfo(
                new AlgorithmIdentifier(ID_ECDSA_SECP256K1),
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
        return keyData;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PublicKeyECDSA publicKey = (PublicKeyECDSA) o;
        return Arrays.equals(keyData, publicKey.keyData);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(keyData);
    }
}
