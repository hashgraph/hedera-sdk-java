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

import com.hedera.hashgraph.sdk.utils.Bip32Utils;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.util.Arrays;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Encapsulate the ECDSA private key.
 */
public class PrivateKeyECDSA extends PrivateKey {

    private final BigInteger keyData;

    @Nullable
    private final KeyParameter chainCode;

    /**
     * Constructor.
     *
     * @param keyData                   the key data
     */
    PrivateKeyECDSA(BigInteger keyData, @Nullable KeyParameter chainCode) {
        this.keyData = keyData;
        this.chainCode = chainCode;
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
        return new PrivateKeyECDSA(privParams.getD(), null);
    }

    /**
     * Create a new private key from a private key ino object.
     *
     * @param privateKeyInfo            the private key info object
     * @return                          the new key
     */
    static PrivateKey fromPrivateKeyInfoInternal(PrivateKeyInfo privateKeyInfo) {
        try {
            var privateKey = ECPrivateKey.getInstance(privateKeyInfo.parsePrivateKey());
            return fromECPrivateKeyInternal(privateKey);
        } catch (IllegalArgumentException e) {
            // Try legacy import
            try {
                var privateKey = (ASN1OctetString) privateKeyInfo.parsePrivateKey();
                return new PrivateKeyECDSA(new BigInteger(1, privateKey.getOctets()), null);
            } catch (IOException ex) {
                throw new BadKeyException(ex);
            }
        } catch (IOException e) {
            throw new BadKeyException(e);
        }
    }

    /**
     * Create a new private key from a ECPrivateKey object.
     *
     * @param privateKey                the ECPrivateKey object
     * @return                          the new key
     */
    static PrivateKey fromECPrivateKeyInternal(ECPrivateKey privateKey) {
        return new PrivateKeyECDSA(privateKey.getKey(), null);
    }

    /**
     * Create a private key from a byte array.
     *
     * @param privateKey                the byte array
     * @return                          the new key
     */
    static PrivateKey fromBytesInternal(byte[] privateKey) {
        if (privateKey.length == 32) {
            return new PrivateKeyECDSA(new BigInteger(1, privateKey), null);
        }

        // Assume a DER-encoded private key descriptor
        return fromECPrivateKeyInternal(ECPrivateKey.getInstance(privateKey));
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
        return this.chainCode != null;
    }

    @Override
    public PrivateKey derive(int index) {
        if (!isDerivable()) {
            throw new IllegalStateException("this private key does not support derivation");
        }

        boolean isHardened = Bip32Utils.isHardenedIndex(index);
        ByteBuffer data = ByteBuffer.allocate(37);

        if (isHardened) {
            byte[] bytes33 = new byte[33];
            byte[] priv = toBytesRaw();
            System.arraycopy(priv, 0, bytes33, 33 - priv.length, priv.length);
            data.put(bytes33);
        } else {
            data.put(getPublicKey().toBytesRaw());
        }
        data.putInt(index);

        byte[] dataArray = data.array();
        HMac hmacSha512 = new HMac(new SHA512Digest());
        hmacSha512.init(new KeyParameter(chainCode.getKey()));
        hmacSha512.update(dataArray, 0, dataArray.length);

        byte[] i = new byte[64];
        hmacSha512.doFinal(i, 0);

        var il = java.util.Arrays.copyOfRange(i, 0, 32);
        var ir = java.util.Arrays.copyOfRange(i, 32, 64);

        var ki = keyData.add(new BigInteger(1, il)).mod(ECDSA_SECP256K1_CURVE.getN());

        return new PrivateKeyECDSA(ki, new KeyParameter(ir));
    }

    /**
     * Create an ECDSA key from seed.
     *
     * @param seed                      the seed bytes
     * @return                          the new key
     */
    public static PrivateKey fromSeed(byte[] seed) {
        var hmacSha512 = new HMac(new SHA512Digest());
        hmacSha512.init(new KeyParameter("Bitcoin seed".getBytes(StandardCharsets.UTF_8)));
        hmacSha512.update(seed, 0, seed.length);

        var derivedState = new byte[hmacSha512.getMacSize()];
        hmacSha512.doFinal(derivedState, 0);

        return derivableKeyECDSA(derivedState);
    }

    /**
     * Create a derived key.
     *
     * @param deriveData                data to derive the key
     * @return                          the new key
     */
    static PrivateKeyECDSA derivableKeyECDSA(byte[] deriveData) {
        var keyData = java.util.Arrays.copyOfRange(deriveData, 0, 32);
        var chainCode = new KeyParameter(deriveData, 32, 32);

        return new PrivateKeyECDSA(new BigInteger(1, keyData), chainCode);
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

    public KeyParameter getChainCode() {
        return chainCode;
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
            return new ECPrivateKey(
                256,
                keyData,
                new DERBitString(getPublicKey().toBytesRaw()),
                new X962Parameters(ID_ECDSA_SECP256K1)
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
