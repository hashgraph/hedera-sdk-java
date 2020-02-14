package com.hedera.hashgraph.sdk.crypto;

import com.hedera.hashgraph.sdk.Internal;

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
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;

import javax.annotation.Nullable;
import javax.crypto.Cipher;

@Internal
public final class PemUtils {
    public static final String TYPE_PRIVATE_KEY = "PRIVATE KEY";
    public static final String TYPE_ENCRYPTED_PRIVATE_KEY = "ENCRYPTED PRIVATE KEY";

    private PemUtils() { }

    /*
     * For some reason, this generates PEM encodings that we ourselves can import, but OpenSSL
     * doesn't like. We decided to punt on generating encrypted PEMs for now but saving
     * the code for when we get back to it and/or any demand arises.
     */
    @SuppressWarnings("unused")
    public static void writeEncryptedPrivateKey(PrivateKeyInfo pkInfo, Writer out, String passphrase) throws IOException {
        byte[] salt = CryptoUtils.randomBytes(CryptoUtils.SALT_LEN);

        KeyParameter derivedKey = CryptoUtils.deriveKeySha256(
            passphrase, salt, CryptoUtils.ITERATIONS, CryptoUtils.CBC_DK_LEN);

        byte[] iv = CryptoUtils.randomBytes(CryptoUtils.IV_LEN);

        Cipher cipher = CryptoUtils.initAesCbc128Encrypt(derivedKey, iv);

        byte[] encryptedKey = CryptoUtils.runCipher(cipher, pkInfo.getEncoded());

        // I wanted to just do this with BC's PKCS8Generator and KcePKCSPBEOutputEncryptorBuilder
        // but it tries to init AES instance of `Cipher` with a `PBKDF2Key` and the former complains

        // So this is basically a reimplementation of that minus the excess OO
        PBES2Parameters parameters = new PBES2Parameters(
            new KeyDerivationFunc(
                PKCSObjectIdentifiers.id_PBKDF2,
                new PBKDF2Params(
                    salt,
                    CryptoUtils.ITERATIONS,
                    CryptoUtils.CBC_DK_LEN,
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

    public static PrivateKeyInfo readPrivateKey(Reader input, @Nullable String passphrase) throws IOException {
        final PemReader pemReader = new PemReader(input);

        PemObject readObject = null;

        for (;;) {
            PemObject nextObject = pemReader.readPemObject();

            if (nextObject == null) break;
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

    private static PrivateKeyInfo decryptPrivateKey(byte[] encodedStruct, String passphrase) throws IOException {
        PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = new PKCS8EncryptedPrivateKeyInfo(encodedStruct);

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
            ? kdfParams.getKeyLength().intValueExact()
            : CryptoUtils.CBC_DK_LEN;

        KeyParameter derivedKey = CryptoUtils.deriveKeySha256(
            passphrase,
            kdfParams.getSalt(),
            kdfParams.getIterationCount().intValueExact(),
            keyLength);

        AlgorithmParameters aesParams;
        try {
            aesParams = AlgorithmParameters.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        aesParams.init(encScheme.getParameters().toASN1Primitive().getEncoded());

        Cipher cipher = CryptoUtils.initAesCbc128Decrypt(derivedKey, aesParams);
        byte[] decrypted = CryptoUtils.runCipher(cipher, encryptedPrivateKeyInfo.getEncryptedData());

        // we need to parse our input data as the cipher may add padding
        ASN1InputStream inputStream = new ASN1InputStream(new ByteArrayInputStream(decrypted));
        return PrivateKeyInfo.getInstance(inputStream.readObject());
    }
}
