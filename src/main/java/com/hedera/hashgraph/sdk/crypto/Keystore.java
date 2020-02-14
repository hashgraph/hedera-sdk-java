package com.hedera.hashgraph.sdk.crypto;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;
import com.hedera.hashgraph.sdk.Internal;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Internal
public final class Keystore {
    private static final Gson gson = new Gson();
    private static final JsonParser jsonParser = new JsonParser();

    private byte[] keyBytes;

    private Keystore(byte[] keyBytes) {
        this.keyBytes = keyBytes;
    }

    public Keystore(PrivateKey<? extends PublicKey> privateKey) {
        this.keyBytes = privateKey.toBytes();
    }

    public static Keystore fromStream(InputStream stream, String passphrase) throws IOException, BadKeyException {
        try {
            final JsonObject jsonObject = jsonParser.parse(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .getAsJsonObject();
            return fromJson(jsonObject, passphrase);
        } catch (IllegalStateException e) {
            throw new BadKeyException(Optional.ofNullable(e.getMessage()).orElse("failed to parse Keystore"));
        } catch (JsonIOException e) {
            // RFC (@abonander): I'm all for keeping this as an unchecked exception
            // but I want consistency with export() so this may involve creating our own exception
            // because JsonIOException is kinda leaking implementation details.
            throw (IOException) Objects.requireNonNull(e.getCause());
        } catch (JsonSyntaxException e) {
            throw new BadKeyException(e);
        }
    }

    /**
     * Get the decoded key from this keystore as an {@link Ed25519PrivateKey}.
     *
     * @throws BadKeyException if the key bytes are of an incorrect length for a raw
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
                throw new BadKeyException("unsupported keystore version: " + version);
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

        final byte[] salt = CryptoUtils.randomBytes(CryptoUtils.SALT_LEN);

        final KeyParameter cipherKey = CryptoUtils.deriveKeySha256(passphrase, salt, CryptoUtils.ITERATIONS, CryptoUtils.DK_LEN);

        final byte[] iv = CryptoUtils.randomBytes(CryptoUtils.IV_LEN);

        final byte[] cipherBytes = CryptoUtils.encryptAesCtr128(cipherKey, iv, keyBytes);

        final byte[] mac = CryptoUtils.calcHmacSha384(cipherKey, cipherBytes);

        final JsonObject cipherParams = new JsonObject();
        cipherParams.addProperty("iv", Hex.toHexString(iv));

        final JsonObject kdfParams = new JsonObject();
        kdfParams.addProperty("dkLen", CryptoUtils.DK_LEN);
        kdfParams.addProperty("salt", Hex.toHexString(salt));
        kdfParams.addProperty("c", CryptoUtils.ITERATIONS);
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
            throw new BadKeyException("unsupported keystore cipher: " + cipher);
        }

        if (!kdf.equals("pbkdf2")) {
            throw new BadKeyException("unsuppported KDF: " + kdf);
        }

        final int dkLen = expectInt(kdfParams, "dkLen");
        final String saltStr = expectString(kdfParams, "salt");
        final int count = expectInt(kdfParams, "c");
        final String prf = expectString(kdfParams, "prf");

        if (!prf.equals("hmac-sha256")) {
            throw new BadKeyException("unsupported KDF hash function: " + prf);
        }

        final byte[] cipherBytes = Hex.decode(ciphertext);
        final byte[] iv = Hex.decode(ivString);
        final byte[] mac = Hex.decode(macString);
        final byte[] salt = Hex.decode(saltStr);

        final KeyParameter cipherKey = CryptoUtils.deriveKeySha256(passphrase, salt, count, dkLen);

        final byte[] testHmac = CryptoUtils.calcHmacSha384(cipherKey, cipherBytes);

        if (!Arrays.equals(mac, testHmac)) {
            throw new BadKeyException("HMAC mismatch; passphrase is incorrect");
        }

        return new Keystore(CryptoUtils.decryptAesCtr128(cipherKey, iv, cipherBytes));
    }

    private static JsonObject expectObject(JsonObject object, String key) {
        try {
            return object.get(key).getAsJsonObject();
        } catch (ClassCastException | NullPointerException e) {
            throw new BadKeyException("expected key '" + key + "' to be an object");
        }
    }

    private static int expectInt(JsonObject object, String key) {
        try {
            return object.get(key).getAsInt();
        } catch (ClassCastException | NullPointerException e) {
            throw new BadKeyException("expected key '" + key + "' to be an integer");
        }
    }

    private static String expectString(JsonObject object, String key) {
        try {
            return object.get(key).getAsString();
        } catch (ClassCastException | NullPointerException e) {
            throw new BadKeyException("expected key '" + key + "' to be a string");
        }
    }

}
