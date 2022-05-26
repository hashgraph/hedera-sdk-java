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

import com.google.errorprone.annotations.Var;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.EncryptedPrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.EncryptionScheme;
import org.bouncycastle.asn1.pkcs.KeyDerivationFunc;
import org.bouncycastle.asn1.pkcs.PBES2Parameters;
import org.bouncycastle.asn1.pkcs.PBKDF2Params;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;

/**
 * Internal utility class for handling PEM objects.
 *
 * Privacy-Enhanced Mail (PEM) is a de facto file format for storing and
 * sending cryptographic keys, certificates, and other data, based on a set of
 * 1993 IETF standards defining "privacy-enhanced mail."
 */
final class Pem {
    private static final String TYPE_PRIVATE_KEY = "PRIVATE KEY";
    private static final String TYPE_ENCRYPTED_PRIVATE_KEY = "ENCRYPTED PRIVATE KEY";

    /**
     * Constructor.
     */
    private Pem() {
    }

    /**
     * For some reason, this generates PEM encodings that we ourselves can import, but OpenSSL
     * doesn't like. We decided to punt on generating encrypted PEMs for now but saving
     * the code for when we get back to it and/or any demand arises.
     */
    @SuppressWarnings("unused")
    static void writeEncryptedPrivateKey(PrivateKeyInfo pkInfo, Writer out, String passphrase) throws IOException {
        byte[] salt = Crypto.randomBytes(Crypto.SALT_LEN);

        KeyParameter derivedKey = Crypto.deriveKeySha256(
            passphrase, salt, Crypto.ITERATIONS, Crypto.CBC_DK_LEN);

        byte[] iv = Crypto.randomBytes(Crypto.IV_LEN);

        Cipher cipher = Crypto.initAesCbc128Encrypt(derivedKey, iv);

        byte[] encryptedKey = Crypto.runCipher(cipher, pkInfo.getEncoded());

        // I wanted to just do this with BC's PKCS8Generator and KcePKCSPBEOutputEncryptorBuilder
        // but it tries to init AES instance of `Cipher` with a `PBKDF2Key` and the former complains

        // So this is basically a reimplementation of that minus the excess OO
        PBES2Parameters parameters = new PBES2Parameters(
            new KeyDerivationFunc(
                PKCSObjectIdentifiers.id_PBKDF2,
                new PBKDF2Params(
                    salt,
                    Crypto.ITERATIONS,
                    Crypto.CBC_DK_LEN,
                    new AlgorithmIdentifier(PKCSObjectIdentifiers.id_hmacWithSHA256))),
            new EncryptionScheme(NISTObjectIdentifiers.id_aes128_CBC,
                ASN1Primitive.fromByteArray(cipher.getParameters().getEncoded())));

        EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new EncryptedPrivateKeyInfo(
            new AlgorithmIdentifier(PKCSObjectIdentifiers.id_PBES2, parameters),
            encryptedKey);

        PemWriter writer = new PemWriter(out);
        writer.writeObject(new PemObject(TYPE_ENCRYPTED_PRIVATE_KEY, encryptedPrivateKeyInfo.getEncoded()));
        writer.flush();
    }

    /**
     * Create a private key info object from a reader.
     *
     * @param input                     reader object
     * @param passphrase                passphrase
     * @return                          private key info object
     * @throws IOException              if IO operations fail
     */
    static PrivateKeyInfo readPrivateKey(Reader input, @Nullable String passphrase) throws IOException {
        PemReader pemReader = new PemReader(input);

        @Var PemObject readObject = null;

        for (; ; ) {
            PemObject nextObject = pemReader.readPemObject();

            if (nextObject == null) {
                break;
            }

            readObject = nextObject;

            String objType = readObject.getType();

            if (passphrase != null && !passphrase.isEmpty() && objType.equals(TYPE_ENCRYPTED_PRIVATE_KEY)) {
                return decryptPrivateKey(readObject.getContent(), passphrase);
            } else if (objType.equals(TYPE_PRIVATE_KEY)) {
                return PrivateKeyInfo.getInstance(readObject.getContent());
            }
        }

        if (readObject != null && readObject.getType().equals(TYPE_ENCRYPTED_PRIVATE_KEY)) {
            throw new BadKeyException("PEM file contained an encrypted private key but no passphrase was given");
        }

        throw new BadKeyException("PEM file did not contain a private key");
    }

    /**
     * Create a private key info object from a byte array.
     *
     * @param encodedStruct             the byte array
     * @param passphrase                passphrase
     * @return                          private key info object
     * @throws IOException              if IO operations fail
     */
    private static PrivateKeyInfo decryptPrivateKey(byte[] encodedStruct, String passphrase) throws IOException {
        var encryptedPrivateKeyInfo = EncryptedPrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(encodedStruct));

        AlgorithmIdentifier encryptAlg = encryptedPrivateKeyInfo.getEncryptionAlgorithm();

        if (!encryptAlg.getAlgorithm().equals(PKCSObjectIdentifiers.id_PBES2)) {
            throw new BadKeyException("unsupported PEM key encryption: " + encryptAlg);
        }

        PBES2Parameters params = PBES2Parameters.getInstance(encryptAlg.getParameters());
        KeyDerivationFunc kdf = params.getKeyDerivationFunc();
        EncryptionScheme encScheme = params.getEncryptionScheme();

        if (!kdf.getAlgorithm().equals(PKCSObjectIdentifiers.id_PBKDF2)) {
            throw new BadKeyException("unsupported KDF: " + kdf.getAlgorithm());
        }

        if (!encScheme.getAlgorithm().equals(NISTObjectIdentifiers.id_aes128_CBC)) {
            throw new BadKeyException("unsupported encryption: " + encScheme.getAlgorithm());
        }

        PBKDF2Params kdfParams = PBKDF2Params.getInstance(kdf.getParameters());

        if (!kdfParams.getPrf().getAlgorithm().equals(PKCSObjectIdentifiers.id_hmacWithSHA256)) {
            throw new BadKeyException("unsupported PRF: " + kdfParams.getPrf());
        }

        int keyLength = kdfParams.getKeyLength() != null
            ? kdfParams.getKeyLength().intValue()
            : Crypto.CBC_DK_LEN;

        KeyParameter derivedKey = Crypto.deriveKeySha256(
            passphrase,
            kdfParams.getSalt(),
            kdfParams.getIterationCount().intValue(),
            keyLength);

        AlgorithmParameters aesParams;
        try {
            aesParams = AlgorithmParameters.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        aesParams.init(encScheme.getParameters().toASN1Primitive().getEncoded());

        Cipher cipher = Crypto.initAesCbc128Decrypt(derivedKey, aesParams);
        byte[] decrypted = Crypto.runCipher(cipher, encryptedPrivateKeyInfo.getEncryptedData());

        // we need to parse our input data as the cipher may add padding
        ASN1InputStream inputStream = new ASN1InputStream(new ByteArrayInputStream(decrypted));
        return PrivateKeyInfo.getInstance(inputStream.readObject());
    }
}
