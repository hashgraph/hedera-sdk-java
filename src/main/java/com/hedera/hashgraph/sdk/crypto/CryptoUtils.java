package com.hedera.hashgraph.sdk.crypto;

import com.hedera.hashgraph.sdk.Internal;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Internal
public final class CryptoUtils {
    /**
     * A {@link SecureRandom} instance for internal use.
     */
    // SecureRandom.getInstanceStrong() is horrible inside Docker (because it blocks forever
    // waiting for entropy) so we avoid it
    public static final SecureRandom secureRandom = new SecureRandom();

    static final int IV_LEN = 16;
    static final int ITERATIONS = 262144;
    static final int SALT_LEN = 32;
    static final int DK_LEN = 32;

    // OpenSSL doesn't like longer derived keys
    static final int CBC_DK_LEN = 16;

    private CryptoUtils() { }

    static KeyParameter deriveKeySha256(String passphrase, byte[] salt, int iterations, int dkLenBytes) {
        final PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(passphrase.getBytes(StandardCharsets.UTF_8), salt, iterations);

        return (KeyParameter) gen.generateDerivedParameters(dkLenBytes * 8);
    }

    static Cipher initAesCtr128(KeyParameter cipherKey, byte[] iv, boolean forDecrypt) {
        final Cipher aesCipher;

        try {
            aesCipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new Error("platform does not support AES-CTR ciphers");
        }

        return initAesCipher(aesCipher, cipherKey, iv, forDecrypt);
    }

    static Cipher initAesCbc128Encrypt(KeyParameter cipherKey, byte[] iv) {
        final Cipher aesCipher;

        try {
            aesCipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new Error("platform does not support AES-CBC ciphers");
        }

        return initAesCipher(aesCipher, cipherKey, iv, false);
    }

    static Cipher initAesCbc128Decrypt(KeyParameter cipherKey, AlgorithmParameters parameters) {
        final Cipher aesCipher;

        try {
            aesCipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new Error("platform does not support AES-CBC ciphers");
        }

        try {
            aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(cipherKey.getKey(), 0, 16, "AES"), parameters);
        } catch (InvalidKeyException e) {
            throw new Error("platform does not support AES-128 ciphers");
        } catch (InvalidAlgorithmParameterException e) {
            throw new Error(e);
        }

        return aesCipher;
    }

    private static Cipher initAesCipher(Cipher aesCipher, KeyParameter cipherKey, byte[] iv, boolean forDecrypt) {
        final int mode = forDecrypt ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE;

        try {
            aesCipher.init(mode, new SecretKeySpec(cipherKey.getKey(), 0, 16, "AES"),
                new IvParameterSpec(iv));
        } catch (InvalidKeyException e) {
            throw new Error("platform does not support AES-128 ciphers");
        } catch (InvalidAlgorithmParameterException e) {
            throw new Error(e);
        }

        return aesCipher;
    }

    static byte[] encryptAesCtr128(KeyParameter cipherKey, byte[] iv, byte[] input) {
        final Cipher aesCipher = initAesCtr128(cipherKey, iv, false);
        return runCipher(aesCipher, input);
    }

    static byte[] decryptAesCtr128(KeyParameter cipherKey, byte[] iv, byte[] input) {
        final Cipher aesCipher = initAesCtr128(cipherKey, iv, true);
        return runCipher(aesCipher, input);
    }

    static byte[] runCipher(Cipher cipher, byte[] input) {
        final byte[] output = new byte[cipher.getOutputSize(input.length)];

        try {
            cipher.doFinal(input, 0, input.length, output);
        } catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
            throw new Error(e);
        }

        return output;
    }

    static byte[] calcHmacSha384(KeyParameter cipherKey, byte[] input) {
        final HMac hmacSha384 = new HMac(new SHA384Digest());
        final byte[] output = new byte[hmacSha384.getMacSize()];

        hmacSha384.init(new KeyParameter(cipherKey.getKey(), 16, 16));
        hmacSha384.update(input, 0, input.length);
        hmacSha384.doFinal(output, 0);

        return output;
    }

    static byte[] randomBytes(int len) {
        final byte[] out = new byte[len];
        secureRandom.nextBytes(out);
        return out;
    }
}
