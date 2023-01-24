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
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.math.ec.rfc8032.Ed25519;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Encapsulate the ED25519 private key.
 */
class PrivateKeyED25519 extends PrivateKey {
    private final byte[] keyData;

    @Nullable
    private final KeyParameter chainCode;

    /**
     * Constructor.
     *
     * @param keyData                   the key data
     * @param chainCode                 the chain code
     */
    PrivateKeyED25519(byte[] keyData, @Nullable KeyParameter chainCode) {
        this.keyData = keyData;
        this.chainCode = chainCode;
    }

    /**
     * Create a new private ED25519 key.
     *
     * @return                          the new key
     */
    static PrivateKeyED25519 generateInternal() {
        // extra 32 bytes for chain code
        byte[] data = new byte[Ed25519.SECRET_KEY_SIZE + 32];
        ThreadLocalSecureRandom.current().nextBytes(data);

        return derivableKeyED25519(data);
    }

    /**
     * Create a new private key from a private key info object.
     *
     * @param privateKeyInfo            the private key info object
     * @return                          the new key
     */
    static PrivateKeyED25519 fromPrivateKeyInfoInternal(PrivateKeyInfo privateKeyInfo) {
        try {
            var privateKey = (ASN1OctetString) privateKeyInfo.parsePrivateKey();

            return new PrivateKeyED25519(privateKey.getOctets(), null);
        } catch (IOException e) {
            throw new BadKeyException(e);
        }
    }

    /**
     * Create an ED25519 key from seed.
     *
     * @param seed                      the seed bytes
     * @return                          the new key
     */
    public static PrivateKey fromSeed(byte[] seed) {
        var hmacSha512 = new HMac(new SHA512Digest());
        hmacSha512.init(new KeyParameter("ed25519 seed".getBytes(StandardCharsets.UTF_8)));
        hmacSha512.update(seed, 0, seed.length);

        var derivedState = new byte[hmacSha512.getMacSize()];
        hmacSha512.doFinal(derivedState, 0);

        return PrivateKeyED25519.derivableKeyED25519(derivedState);
    }

    /**
     * Create a derived key.
     *
     * @param deriveData                data to derive the key
     * @return                          the new key
     */
    static PrivateKeyED25519 derivableKeyED25519(byte[] deriveData) {
        var keyData = Arrays.copyOfRange(deriveData, 0, 32);
        var chainCode = new KeyParameter(deriveData, 32, 32);

        return new PrivateKeyED25519(keyData, chainCode);
    }

    /**
     * Create a private key from a byte array.
     *
     * @param privateKey                the byte array
     * @return                          the new key
     */
    public static PrivateKey fromBytesInternal(byte[] privateKey) {
        if ((privateKey.length == Ed25519.SECRET_KEY_SIZE)
            || (privateKey.length == Ed25519.SECRET_KEY_SIZE + Ed25519.PUBLIC_KEY_SIZE)) {
            // If this is a 32 or 64 byte string, assume an Ed25519 private key
            return new PrivateKeyED25519(Arrays.copyOfRange(privateKey, 0, Ed25519.SECRET_KEY_SIZE), null);
        }

        // Assume a DER-encoded private key descriptor
        return fromPrivateKeyInfoInternal(PrivateKeyInfo.getInstance(privateKey));
    }

    /**
     * Derive a legacy child key.
     *
     * @param entropy                   entropy byte array
     * @param index                     the child key index
     * @return                          the new key
     */
    static byte[] legacyDeriveChildKey(byte[] entropy, long index) {
        byte[] seed = new byte[entropy.length + 8];
        Arrays.fill(seed, 0, seed.length, (byte) 0);
        if (index == 0xffffffffffL) {
            seed[entropy.length + 3] = (byte) 0xff;
            Arrays.fill(seed, entropy.length + 4, seed.length, (byte) (index >>> 32));
        } else {
            if (index < 0) {
                Arrays.fill(seed, entropy.length, entropy.length + 4, (byte) -1);
            } else {
                Arrays.fill(seed, entropy.length, entropy.length + 4, (byte) 0);
            }
            Arrays.fill(seed, entropy.length + 4, seed.length, Long.valueOf(index).byteValue());
        }
        System.arraycopy(entropy, 0, seed, 0, entropy.length);

        byte[] salt = new byte[1];
        salt[0] = -1;
        PKCS5S2ParametersGenerator pbkdf2 = new PKCS5S2ParametersGenerator(new SHA512Digest());
        pbkdf2.init(
            seed,
            salt,
            2048);

        KeyParameter key = (KeyParameter) pbkdf2.generateDerivedParameters(256);
        return key.getKey();
    }

    @Override
    public PrivateKey legacyDerive(long index) {
        var keyBytes = legacyDeriveChildKey(this.keyData, index);

        return fromBytesInternal(keyBytes);
    }

    @Override
    public boolean isDerivable() {
        return this.chainCode != null;
    }

    @Override
    public PrivateKey derive(int index) {
        if (this.chainCode == null) {
            throw new IllegalStateException("this private key does not support derivation");
        }

        if ((index & 0x80000000) != 0) {
            throw new IllegalArgumentException("the index should not be pre-hardened");
        }

        // SLIP-10 child key derivation
        // https://github.com/satoshilabs/slips/blob/master/slip-0010.md#master-key-generation
        var hmacSha512 = new HMac(new SHA512Digest());

        hmacSha512.init(chainCode);
        hmacSha512.update((byte) 0);

        hmacSha512.update(keyData, 0, Ed25519.SECRET_KEY_SIZE);

        // write the index in big-endian order, setting the 31st bit to mark it "hardened"
        var indexBytes = new byte[4];
        ByteBuffer.wrap(indexBytes).order(ByteOrder.BIG_ENDIAN).putInt(index);
        indexBytes[0] |= (byte) 0b10000000;

        hmacSha512.update(indexBytes, 0, indexBytes.length);

        var output = new byte[64];
        hmacSha512.doFinal(output, 0);

        return derivableKeyED25519(output);
    }

    @Override
    public PublicKey getPublicKey() {
        if (publicKey != null) {
            return publicKey;
        }

        byte[] publicKeyData = new byte[Ed25519.PUBLIC_KEY_SIZE];
        Ed25519.generatePublicKey(keyData, 0, publicKeyData, 0);

        publicKey = new PublicKeyED25519(publicKeyData);
        return publicKey;
    }

    public KeyParameter getChainCode() {
        return chainCode;
    }

    @Override
    public byte[] sign(byte[] message) {
        byte[] signature = new byte[Ed25519.SIGNATURE_SIZE];
        Ed25519.sign(keyData, 0, message, 0, message.length, signature, 0);

        return signature;
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
    public byte[] toBytesDER() {
        try {
            return new PrivateKeyInfo(
                new AlgorithmIdentifier(ID_ED25519),
                new DEROctetString(keyData)
            ).getEncoded("DER");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
