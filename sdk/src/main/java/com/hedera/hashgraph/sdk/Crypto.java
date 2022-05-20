package com.hedera.hashgraph.sdk;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jcajce.provider.digest.Keccak;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class used internally by the sdk.
 */
final class Crypto {
    static final int IV_LEN = 16;
    static final int ITERATIONS = 262144;
    static final int SALT_LEN = 32;
    static final int DK_LEN = 32;

    // OpenSSL doesn't like longer derived keys
    static final int CBC_DK_LEN = 16;

    /**
     * Constructor.
     */
    private Crypto() {
    }

    /**
     * Derive a sha 256 key.
     *
     * @param passphrase                the password will be converted into bytes
     * @param salt                      the salt to be mixed in
     * @param iterations                the iterations for mixing
     * @param dkLenBytes                the key length in bytes
     * @return                          the key parameter object
     */
    static KeyParameter deriveKeySha256(String passphrase, byte[] salt, int iterations, int dkLenBytes) {
        PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(passphrase.getBytes(StandardCharsets.UTF_8), salt, iterations);

        return (KeyParameter) gen.generateDerivedParameters(dkLenBytes * 8);
    }

    /**
     * Initialize an advanced encryption standard counter mode cipher.
     *
     * @param cipherKey                 the cipher key
     * @param iv                        the initialization vector byte array
     * @param forDecrypt                is this for decryption
     * @return                          the aes ctr cipher
     */
    static Cipher initAesCtr128(KeyParameter cipherKey, byte[] iv, boolean forDecrypt) {
        Cipher aesCipher;

        try {
            aesCipher = Cipher.getInstance("AES/CTR/NOPADDING");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new Error("platform does not support AES-CTR ciphers", e);
        }

        return initAesCipher(aesCipher, cipherKey, iv, forDecrypt);
    }

    /**
     * Initialize an advanced encryption standard cipher block chaining mode
     * cipher for encryption.
     *
     * @param cipherKey                 the cipher key
     * @param iv                        the initialization vector byte array
     * @return                          the aes cbc cipher
     */
    static Cipher initAesCbc128Encrypt(KeyParameter cipherKey, byte[] iv) {
        Cipher aesCipher;

        try {
            aesCipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new Error("platform does not support AES-CBC ciphers", e);
        }

        return initAesCipher(aesCipher, cipherKey, iv, false);
    }

    /**
     * Initialize an advanced encryption standard cipher block chaining mode
     * cipher for decryption.
     *
     * @param cipherKey                 the cipher key
     * @param parameters                the algorithm parameters
     * @return                          the aes cbc cipher
     */
    static Cipher initAesCbc128Decrypt(KeyParameter cipherKey, AlgorithmParameters parameters) {
        Cipher aesCipher;

        try {
            aesCipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new Error("platform does not support AES-CBC ciphers", e);
        }

        try {
            aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(cipherKey.getKey(), 0, 16, "AES"), parameters);
        } catch (InvalidKeyException e) {
            throw new Error("platform does not support AES-128 ciphers", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new Error(e);
        }

        return aesCipher;
    }

    /**
     * Create a new aes cipher.
     *
     * @param aesCipher                 the aes cipher
     * @param cipherKey                 the cipher key
     * @param iv                        the initialization vector byte array
     * @param forDecrypt                is this for decryption True or encryption False
     * @return                          the new aes cipher
     */
    private static Cipher initAesCipher(Cipher aesCipher, KeyParameter cipherKey, byte[] iv, boolean forDecrypt) {
        int mode = forDecrypt ? Cipher.DECRYPT_MODE : Cipher.ENCRYPT_MODE;

        try {
            aesCipher.init(mode, new SecretKeySpec(cipherKey.getKey(), 0, 16, "AES"),
                new IvParameterSpec(iv));
        } catch (InvalidKeyException e) {
            throw new Error("platform does not support AES-128 ciphers", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new Error(e);
        }

        return aesCipher;
    }

    /**
     * Encrypt a byte array with the aes ctr cipher.
     *
     * @param cipherKey                 the cipher key
     * @param iv                        the initialization vector
     * @param input                     the byte array to encrypt
     * @return                          the encrypted byte array
     */
    static byte[] encryptAesCtr128(KeyParameter cipherKey, byte[] iv, byte[] input) {
        Cipher aesCipher = initAesCtr128(cipherKey, iv, false);
        return runCipher(aesCipher, input);
    }

    /**
     * Decrypt a byte array with the aes ctr cipher.
     *
     * @param cipherKey                 the cipher key
     * @param iv                        the initialization vector
     * @param input                     the byte array to decrypt
     * @return                          the decrypted byte array
     */
    static byte[] decryptAesCtr128(KeyParameter cipherKey, byte[] iv, byte[] input) {
        Cipher aesCipher = initAesCtr128(cipherKey, iv, true);
        return runCipher(aesCipher, input);
    }

    /**
     * Run the cipher on the given input.
     *
     * @param cipher                    the cipher
     * @param input                     the byte array
     * @return                          the output of running the cipher
     */
    static byte[] runCipher(Cipher cipher, byte[] input) {
        byte[] output = new byte[cipher.getOutputSize(input.length)];

        try {
            cipher.doFinal(input, 0, input.length, output);
        } catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
            throw new Error(e);
        }

        return output;
    }

    /**
     * Calculate a hash message authentication code using the secure hash
     * algorithm variant 384.
     *
     * @param cipherKey                 the cipher key
     * @param iv                        the initialization vector
     * @param input                     the byte array
     * @return                          the hmac using sha 384
     */
    static byte[] calcHmacSha384(KeyParameter cipherKey, @Nullable byte[] iv, byte[] input) {
        HMac hmacSha384 = new HMac(new SHA384Digest());
        byte[] output = new byte[hmacSha384.getMacSize()];

        hmacSha384.init(new KeyParameter(cipherKey.getKey(), 16, 16));
        if (iv != null) {
            hmacSha384.update(iv, 0, iv.length);
        }
        hmacSha384.update(input, 0, input.length);
        hmacSha384.doFinal(output, 0);

        return output;
    }

    /**
     * Calculate a keccak 256-bit hash.
     *
     * @param message                   the message to be hashed
     * @return                          the hash
     */
    static byte[] calcKeccak256(byte[] message) {
        var digest = new Keccak.Digest256();
        digest.update(message);
        return digest.digest();
    }

    /**
     * Generate some randomness.
     *
     * @param len                       the number of bytes requested
     * @return                          the byte array of randomness
     */
    static byte[] randomBytes(int len) {
        byte[] out = new byte[len];
        ThreadLocalSecureRandom.current().nextBytes(out);

        return out;
    }
}
