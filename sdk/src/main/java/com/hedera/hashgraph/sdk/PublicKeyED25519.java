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
import org.bouncycastle.math.ec.rfc8032.Ed25519;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;

/**
 * Encapsulate the ED25519 public key.
 */
class PublicKeyED25519 extends PublicKey {
    private final byte[] keyData;

    /**
     * Constructor.
     *
     * @param keyData                   the byte array representing the key
     */
    PublicKeyED25519(byte[] keyData) {
        this.keyData = keyData;
    }

    /**
     * Create a key from a byte array representation.
     *
     * @param publicKey                 the byte array representing the key
     * @return                          the new key
     */
    static PublicKeyED25519 fromBytesInternal(byte[] publicKey) {
        if (publicKey.length == Ed25519.PUBLIC_KEY_SIZE) {
            // If this is a 32 byte string, assume an Ed25519 public key
            return new PublicKeyED25519(publicKey);
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
    static PublicKeyED25519 fromSubjectKeyInfoInternal(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        return new PublicKeyED25519(subjectPublicKeyInfo.getPublicKeyData().getBytes());
    }

    @Override
    ByteString extractSignatureFromProtobuf(SignaturePair pair) {
        return pair.getEd25519();
    }

    @Override
    public boolean verify(byte[] message, byte[] signature) {
        return Ed25519.verify(signature, 0, keyData, 0, message, 0, message.length);
    }

    @Override
    com.hedera.hashgraph.sdk.proto.Key toProtobufKey() {
        return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setEd25519(ByteString.copyFrom(keyData))
            .build();
    }

    @Override
    SignaturePair toSignaturePairProtobuf(byte[] signature) {
        return SignaturePair.newBuilder()
            .setPubKeyPrefix(ByteString.copyFrom(keyData))
            .setEd25519(ByteString.copyFrom(signature))
            .build();
    }

    @Override
    public byte[] toBytesDER() {
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
        return toBytesRaw();
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

        PublicKeyED25519 publicKey = (PublicKeyED25519) o;
        return Arrays.equals(keyData, publicKey.keyData);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(keyData);
    }

    @Override
    public boolean isED25519() {
        return true;
    }

    @Override
    public boolean isECDSA() {
        return false;
    }
}
