package com.hedera.hashgraph.sdk.crypto;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;
import com.hedera.hashgraph.sdk.Internal;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Internal
public final class Keystore {
    private static final Gson gson = new Gson();
    private static final JsonParser jsonParser = new JsonParser();

    private static final int IV_LEN = 16;
    private static final int COUNT = 262144;
    private static final int SALT_LEN = 32;
    private static final int DK_LEN = 32;

    private byte[] keyBytes;

    private Keystore(byte[] keyBytes) {
        this.keyBytes = keyBytes;
    }

    public Keystore(PrivateKey privateKey) {
        this.keyBytes = privateKey.toBytes();
    }

    public static Keystore fromStream(InputStream stream, String passphrase) throws IOException, KeystoreParseException {
        try {
            final JsonObject jsonObject = jsonParser.parse(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .getAsJsonObject();
            return fromJson(jsonObject, passphrase);
        } catch (IllegalStateException e) {
            throw new KeystoreParseException(Optional.ofNullable(e.getMessage()).orElse("failed to parse Keystore"));
        } catch (JsonIOException e) {
            // RFC (@abonander): I'm all for keeping this as an unchecked exception
            // but I want consistency with export() so this may involve creating our own exception
            // because JsonIOException is kinda leaking implementation details.
            throw (IOException) Objects.requireNonNull(e.getCause());
        } catch (JsonSyntaxException e) {
            throw new KeystoreParseException(e);
        }
    }

    /**
     * Get the decoded key from this keystore as an {@link Ed25519PrivateKey}.
     *
     * @throws IllegalArgumentException if the key bytes are of an incorrect length for a raw
     * private key or private key + public key, or do not represent a DER encoded Ed25519
     * private key.
     */
    public Ed25519PrivateKey getEd25519() {
        return Ed25519PrivateKey.fromBytes(keyBytes);
    }

    private static Keystore fromJson(JsonObject object, String passphrase) {
        final int version = expectInt(object, "version");

        //noinspection SwitchStatementWithTooFewBranches
        switch (version) {
            case 1:
                return parseKeystoreV1(expectObject(object, "crypto"), passphrase);
            default:
                throw new KeystoreParseException("unsupported keystore version: " + version);
        }
    }

    public void export(OutputStream outputStream, String passphrase) throws IOException {
        final JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        gson.toJson(exportJson(passphrase), writer);
        writer.flush();
    }

    private JsonObject exportJson(String passphrase) {
        final JsonObject object = new JsonObject();
        object.addProperty("version", 1);

        final JsonObject crypto = new JsonObject();
        crypto.addProperty("cipher", "aes-128-ctr");
        crypto.addProperty("kdf", "pbkdf2");

        final byte[] salt = randomBytes(SALT_LEN);

        final KeyParameter cipherKey = deriveKeySha256(passphrase, salt, COUNT, DK_LEN);

        final byte[] iv = randomBytes(IV_LEN);

        final byte[] cipherBytes = encrypt(cipherKey, iv, keyBytes);

        final byte[] mac = calcHmacSha384(cipherKey, cipherBytes);

        final JsonObject cipherParams = new JsonObject();
        cipherParams.addProperty("iv", Hex.toHexString(iv));

        final JsonObject kdfParams = new JsonObject();
        kdfParams.addProperty("dkLen", DK_LEN);
        kdfParams.addProperty("salt", Hex.toHexString(salt));
        kdfParams.addProperty("c", COUNT);
        kdfParams.addProperty("prf", "hmac-sha256");

        crypto.add("cipherparams", cipherParams);
        crypto.addProperty("ciphertext", Hex.toHexString(cipherBytes));
        crypto.add("kdfparams", kdfParams);
        crypto.addProperty("mac", Hex.toHexString(mac));

        object.add("crypto", crypto);

        return object;
    }

    private static Keystore parseKeystoreV1(JsonObject crypto, String passphrase) {
        final String ciphertext = expectString(crypto, "ciphertext");
        final String ivString = expectString(expectObject(crypto, "cipherparams"), "iv");
        final String cipher = expectString(crypto, "cipher");
        final String kdf = expectString(crypto, "kdf");
        final JsonObject kdfParams = expectObject(crypto, "kdfparams");
        final String macString = expectString(crypto, "mac");

        if (!cipher.equals("aes-128-ctr")) {
            throw new KeystoreParseException("unsupported keystore cipher: " + cipher);
        }

        if (!kdf.equals("pbkdf2")) {
            throw new KeystoreParseException("unsuppported KDF: " + kdf);
        }

        final int dkLen = expectInt(kdfParams, "dkLen");
        final String saltStr = expectString(kdfParams, "salt");
        final int count = expectInt(kdfParams, "c");
        final String prf = expectString(kdfParams, "prf");

        if (!prf.equals("hmac-sha256")) {
            throw new KeystoreParseException("unsupported KDF hash function: " + prf);
        }

        final byte[] cipherBytes = Hex.decode(ciphertext);
        final byte[] iv = Hex.decode(ivString);
        final byte[] mac = Hex.decode(macString);
        final byte[] salt = Hex.decode(saltStr);

        final KeyParameter cipherKey = deriveKeySha256(passphrase, salt, count, dkLen);

        final byte[] testHmac = calcHmacSha384(cipherKey, cipherBytes);

        if (!Arrays.equals(mac, testHmac)) {
            throw new KeystoreParseException("HMAC mismatch; passphrase is incorrect");
        }

        return new Keystore(decrypt(cipherKey, iv, cipherBytes));
    }

    private static JsonObject expectObject(JsonObject object, String key) {
        try {
            return object.get(key).getAsJsonObject();
        } catch (ClassCastException | NullPointerException e) {
            throw new KeystoreParseException("expected key '" + key + "' to be an object");
        }
    }

    private static int expectInt(JsonObject object, String key) {
        try {
            return object.get(key).getAsInt();
        } catch (ClassCastException | NullPointerException e) {
            throw new KeystoreParseException("expected key '" + key + "' to be an integer");
        }
    }

    private static String expectString(JsonObject object, String key) {
        try {
            return object.get(key).getAsString();
        } catch (ClassCastException | NullPointerException e) {
            throw new KeystoreParseException("expected key '" + key + "' to be a string");
        }
    }

    private static KeyParameter deriveKeySha256(String passphrase, byte[] salt, int count, int dkLenBytes) {
        final PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator(new SHA256Digest());
        gen.init(passphrase.getBytes(StandardCharsets.UTF_8), salt, count);

        return (KeyParameter) gen.generateDerivedParameters(dkLenBytes * 8);
    }

    private static Cipher initAesCipher(KeyParameter cipherKey, byte[] iv, boolean forDecrypt) {
        final Cipher aesCipher;

        try {
            aesCipher = Cipher.getInstance("AES/CTR/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new Error("platform does not support AES-CTR ciphers");
        }

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

    private static byte[] encrypt(KeyParameter cipherKey, byte[] iv, byte[] input) {
        final Cipher aesCipher = initAesCipher(cipherKey, iv, false);
        return runCipher(aesCipher, input);
    }

    private static byte[] decrypt(KeyParameter cipherKey, byte[] iv, byte[] input) {
        final Cipher aesCipher = initAesCipher(cipherKey, iv, true);
        return runCipher(aesCipher, input);
    }

    private static byte[] runCipher(Cipher cipher, byte[] input) {
        final byte[] output = new byte[cipher.getOutputSize(input.length)];

        try {
            cipher.doFinal(input, 0, input.length, output);
        } catch (ShortBufferException | IllegalBlockSizeException | BadPaddingException e) {
            throw new Error(e);
        }

        return output;
    }

    private static byte[] calcHmacSha384(KeyParameter cipherKey, byte[] input) {
        final HMac hmacSha384 = new HMac(new SHA384Digest());
        final byte[] output = new byte[hmacSha384.getMacSize()];

        hmacSha384.init(new KeyParameter(cipherKey.getKey(), 16, 16));
        hmacSha384.update(input, 0, input.length);
        hmacSha384.doFinal(output, 0);

        return output;
    }

    private static byte[] randomBytes(int len) {
        final byte[] out = new byte[len];
        PrivateKey.secureRandom.nextBytes(out);
        return out;
    }
}
