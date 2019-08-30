package com.hedera.hashgraph.sdk.crypto.ed25519;

import com.hedera.hashgraph.sdk.crypto.PrivateKey;

import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.SecureRandom;

import javax.annotation.Nullable;

/**
 * An ed25519 private key.
 *
 * <p>To obtain an instance, see {@link #generate()} or {@link #fromString(String)}.
 */
@SuppressWarnings("Duplicates")
public final class Ed25519PrivateKey extends PrivateKey {
    public static final String TYPE_PRIVATE_KEY = "PRIVATE KEY";
    final Ed25519PrivateKeyParameters privKeyParams;
    // computed from private key and memoized
    @Nullable
    private Ed25519PublicKey publicKey;

    private Ed25519PrivateKey(Ed25519PrivateKeyParameters privKeyParams) {
        this.privKeyParams = privKeyParams;
    }

    private Ed25519PrivateKey(Ed25519PrivateKeyParameters privKeyParams, @Nullable Ed25519PublicKeyParameters pubKeyParams) {
        this.privKeyParams = privKeyParams;

        if (pubKeyParams != null) {
            this.publicKey = new Ed25519PublicKey(pubKeyParams);
        }
    }

    /**
     * Construct an Ed25519PrivateKey from a raw byte array.
     *
     * @throws IllegalArgumentException if the key bytes are of an incorrect length for a raw
     * private key or private key + public key, or do not represent a DER encoded Ed25519
     * private key.
     */
    public static Ed25519PrivateKey fromBytes(byte[] keyBytes) {
        Ed25519PrivateKeyParameters privKeyParams;
        Ed25519PublicKeyParameters pubKeyParams = null;

        if (keyBytes.length == Ed25519.SECRET_KEY_SIZE) {
            // if the decoded bytes matches the length of a private key, try that
            privKeyParams = new Ed25519PrivateKeyParameters(keyBytes, 0);
        } else if (keyBytes.length == Ed25519.SECRET_KEY_SIZE + Ed25519.PUBLIC_KEY_SIZE) {
            // some legacy code delivers private and public key pairs concatted together
            try {
                // this is how we read only the first 32 bytes
                privKeyParams = new Ed25519PrivateKeyParameters(new ByteArrayInputStream(keyBytes, 0, Ed25519.SECRET_KEY_SIZE));
                // read the remaining 32 bytes as the public key
                pubKeyParams = new Ed25519PublicKeyParameters(keyBytes, Ed25519.SECRET_KEY_SIZE);

                return new Ed25519PrivateKey(privKeyParams, pubKeyParams);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        } else {
            // decode a properly DER-encoded private key descriptor
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(keyBytes);

            try {
                ASN1Encodable privateKey = privateKeyInfo.parsePrivateKey();
                privKeyParams = new Ed25519PrivateKeyParameters(((ASN1OctetString) privateKey).getOctets(), 0);

                ASN1BitString pubKeyData = privateKeyInfo.getPublicKeyData();

                if (pubKeyData != null) {
                    pubKeyParams = new Ed25519PublicKeyParameters(pubKeyData.getOctets(), 0);
                }

            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        return new Ed25519PrivateKey(privKeyParams, pubKeyParams);
    }

    /**
     * Parse a private key from a PEM encoded file using the default system charset to decode.
     *
     * This will read the first "PRIVATE KEY" section in the file as an Ed25519 private key.
     *
     * @throws IOException if one occurred while reading or if no "PRIVATE KEY" section was found
     */
    public static Ed25519PrivateKey fromPemFile(File pemFile) throws IOException {
        return fromPemFile(pemFile, Charset.defaultCharset());
    }

    /**
     * Parse a private key from a PEM encoded file using the given {@link Charset} to decode.
     *
     * This will read the first "PRIVATE KEY" section in the file as an Ed25519 private key.
     *
     * @throws IOException if one occurred while reading or if no "PRIVATE KEY" section was found
     */
    public static Ed25519PrivateKey fromPemFile(File pemFile, Charset charset) throws IOException {
        return fromPemFile(new InputStreamReader(new FileInputStream(pemFile), charset));
    }

    /**
     * Parse a private key from a PEM encoded reader.
     *
     * This will read the first "PRIVATE KEY" section in the stream as an Ed25519 private key.
     *
     * @throws IOException if one occurred while reading or if no "PRIVATE KEY" section was found
     */
    public static Ed25519PrivateKey fromPemFile(Reader pemFile) throws IOException {
        final PemReader pemReader = new PemReader(pemFile);

        PemObject readObject;

        do {
            readObject = pemReader.readPemObject();
        } while (readObject != null && !readObject.getType().equals(TYPE_PRIVATE_KEY));

        if (readObject != null && readObject.getType().equals(TYPE_PRIVATE_KEY)) {
            return fromBytes(readObject.getContent());
        }

        throw new IOException("pem file did not contain a private key");
    }

    /**
     * Recover a private key from its text-encoded representation.
     *
     * @param privateKeyString the hex-encoded private key string
     * @return the restored private key
     * @throws org.bouncycastle.util.encoders.DecoderException if the hex string is invalid
     * @throws RuntimeException if the decoded key was invalid
     */
    public static Ed25519PrivateKey fromString(String privateKeyString) {
        // TODO: catch unchecked `DecoderException`
        byte[] keyBytes = Hex.decode(privateKeyString);
        return fromBytes(keyBytes);
    }

    /** @return a new private key using {@link java.security.SecureRandom} */
    public static Ed25519PrivateKey generate() {
        return generate(new SecureRandom());
    }

    /** @return a new private key using the given {@link java.security.SecureRandom} */
    public static Ed25519PrivateKey generate(SecureRandom secureRandom) {
        return new Ed25519PrivateKey(new Ed25519PrivateKeyParameters(secureRandom));
    }

    /** @return the public key counterpart of this private key to share with the hashgraph */
    public Ed25519PublicKey getPublicKey() {
        if (publicKey == null) {
            publicKey = new Ed25519PublicKey(privKeyParams.generatePublicKey());
        }

        return publicKey;
    }

    @Override
    protected byte[] toBytes() {
        return privKeyParams.getEncoded();
    }

    private byte[] encodeDER() {
        PrivateKeyInfo privateKeyInfo;

        try {
            privateKeyInfo = new PrivateKeyInfo(
                new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), new DEROctetString(privKeyParams.getEncoded()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            return privateKeyInfo.getEncoded("DER");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return Hex.toHexString(encodeDER());
    }

    /** Write out a PEM encoded version of this private key. */
    public void writePem(Writer out) throws IOException {
        final PemWriter pemWriter = new PemWriter(out);
        pemWriter.writeObject(new PemObject(TYPE_PRIVATE_KEY, encodeDER()));
        pemWriter.flush();
    }
}
