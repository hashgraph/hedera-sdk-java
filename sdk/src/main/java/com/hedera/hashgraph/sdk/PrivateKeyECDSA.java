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

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.util.Arrays;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Encapsulate the ECDSA private key.
 */
public class PrivateKeyECDSA extends PrivateKey {

    private final BigInteger keyData;

    /**
     * Constructor.
     *
     * @param keyData                   the key data
     * @param publicKey                 the public key
     */
    PrivateKeyECDSA(BigInteger keyData, @Nullable PublicKey publicKey) {
        this.keyData = keyData;
        this.publicKey = publicKey;
    }

    /**
     * Create a new private ECDSA key.
     *
     * @return                          the new key
     */
    static PrivateKeyECDSA generateInternal() {
        var generator = new ECKeyPairGenerator();
        var keygenParams = new ECKeyGenerationParameters(ECDSA_SECP256K1_DOMAIN, ThreadLocalSecureRandom.current());
        generator.init(keygenParams);
        var keypair = generator.generateKeyPair();
        var privParams = (ECPrivateKeyParameters) keypair.getPrivate();
        var pubParams = (ECPublicKeyParameters) keypair.getPublic();
        return new PrivateKeyECDSA(privParams.getD(), new PublicKeyECDSA(pubParams.getQ().getEncoded(true)));
    }

    /**
     * Create a new private key from a private key ino object.
     *
     * @param privateKeyInfo            the private key info object
     * @return                          the new key
     */
    static PrivateKeyECDSA fromPrivateKeyInfoInternal(PrivateKeyInfo privateKeyInfo) {
        try {
            var privateKey = (ASN1OctetString) privateKeyInfo.parsePrivateKey();

            return new PrivateKeyECDSA(new BigInteger(1, privateKey.getOctets()), null);
        } catch (IOException e) {
            throw new BadKeyException(e);
        }
    }

    /**
     * Create a private key from a byte array.
     *
     * @param privateKey                the byte array
     * @return                          the new key
     */
    public static PrivateKey fromBytesInternal(byte[] privateKey) {
        if (privateKey.length == 32) {
            return new PrivateKeyECDSA(new BigInteger(1, privateKey), null);
        }

        // Assume a DER-encoded private key descriptor
        return fromPrivateKeyInfoInternal(PrivateKeyInfo.getInstance(privateKey));
    }

    /**
     * Throws an exception when trying to derive a child key.
     *
     * @param entropy                   entropy byte array
     * @param index                     the child key index
     * @return                          the new key
     */
    static byte[] legacyDeriveChildKey(byte[] entropy, long index) {
        throw new IllegalStateException("ECDSA secp256k1 keys do not currently support derivation");
    }

    @Override
    public PrivateKey legacyDerive(long index) {
        throw new IllegalStateException("ECDSA secp256k1 keys do not currently support derivation");
    }

    @Override
    public boolean isDerivable() {
        return false;
    }

    @Override
    public PrivateKey derive(int index) {
        throw new IllegalStateException("ECDSA secp256k1 keys do not currently support derivation");
    }

    @Override
    public PublicKey getPublicKey() {
        if (publicKey != null) {
            return publicKey;
        }

        var q = ECDSA_SECP256K1_DOMAIN.getG().multiply(keyData);
        var publicParams = new ECPublicKeyParameters(q, ECDSA_SECP256K1_DOMAIN);
        publicKey = new PublicKeyECDSA(publicParams.getQ().getEncoded(true));
        return publicKey;
    }

    @Override
    public byte[] sign(byte[] message) {
        var hash = Crypto.calcKeccak256(message);

        var signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
        signer.init(true, new ECPrivateKeyParameters(keyData, ECDSA_SECP256K1_DOMAIN));
        BigInteger[] bigSig = signer.generateSignature(hash);

        byte[] sigBytes = Arrays.copyOf(bigIntTo32Bytes(bigSig[0]), 64);
        System.arraycopy(bigIntTo32Bytes(bigSig[1]), 0, sigBytes, 32, 32);

        return sigBytes;
    }

    @Override
    public byte[] toBytes() {
        return toBytesDER();
    }

    /**
     * Create a big int byte array.
     *
     * @param n                         the big integer
     * @return                          the 32 byte array
     */
    private static byte[] bigIntTo32Bytes(BigInteger n) {
        byte[] bytes = n.toByteArray();
        byte[] bytes32 = new byte[32];
        System.arraycopy(
            bytes, Math.max(0, bytes.length - 32),
            bytes32, Math.max(0, 32 - bytes.length),
            Math.min(32, bytes.length)
        );
        return bytes32;
    }

    @Override
    public byte[] toBytesRaw() {
        return bigIntTo32Bytes(keyData);
    }

    @Override
    public byte[] toBytesDER() {
        try {
            return new PrivateKeyInfo(
                new AlgorithmIdentifier(ID_ECDSA_SECP256K1),
                new DEROctetString(toBytesRaw())
            ).getEncoded("DER");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
