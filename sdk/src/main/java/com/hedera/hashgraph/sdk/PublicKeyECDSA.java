/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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

/**
 * Encapsulate the ECDSA public key.
 */
public class PublicKeyECDSA extends PublicKey {
    // Compressed 33 byte form
    private byte[] keyData;

    /**
     * Constructor.
     *
     * @param keyData                   the byte array representing the key
     */
    PublicKeyECDSA(byte[] keyData) {
        this.keyData = keyData;
    }

    /**
     * Create a key from a byte array representation.
     *
     * @param publicKey                 the byte array representing the key
     * @return                          the new key
     */
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

    /**
     * Create a key from a subject public key info object.
     *
     * @param subjectPublicKeyInfo      the subject public key info object
     * @return                          the new public key
     */
    static PublicKeyECDSA fromSubjectKeyInfoInternal(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        return new PublicKeyECDSA(subjectPublicKeyInfo.getPublicKeyData().getBytes());
    }

    @Override
    ByteString extractSignatureFromProtobuf(SignaturePair pair) {
        return pair.getECDSA384();
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
        return Arrays.copyOf(keyData, keyData.length);
    }

    @Override
    public boolean equals( Object o) {
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

    @Override
    public boolean isED25519() {
        return false;
    }

    @Override
    public boolean isECDSA() {
        return true;
    }
}
