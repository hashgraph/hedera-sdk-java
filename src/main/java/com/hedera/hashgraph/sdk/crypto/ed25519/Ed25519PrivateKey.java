package com.hedera.hashgraph.sdk.crypto.ed25519;

import com.hedera.hashgraph.sdk.crypto.BadKeyException;
import com.hedera.hashgraph.sdk.crypto.CryptoUtils;
import com.hedera.hashgraph.sdk.crypto.Keystore;
import com.hedera.hashgraph.sdk.crypto.Mnemonic;
import com.hedera.hashgraph.sdk.crypto.PemUtils;
import com.hedera.hashgraph.sdk.crypto.PrivateKey;

import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.annotation.Nullable;

/**
 * An ed25519 private key.
 *
 * <p>To obtain an instance, see {@link #generate()} or {@link #fromString(String)}.
 */
@SuppressWarnings("Duplicates")
public final class Ed25519PrivateKey extends PrivateKey<Ed25519PublicKey> {

    final Ed25519PrivateKeyParameters privKeyParams;

    @Nullable
    private final KeyParameter chainCode;

    private Ed25519PrivateKey(Ed25519PrivateKeyParameters privKeyParams) {
        super(new Ed25519PublicKey(privKeyParams.generatePublicKey()));
        this.privKeyParams = privKeyParams;
        this.chainCode = null;
    }

    private Ed25519PrivateKey(Ed25519PrivateKeyParameters privKeyParams, KeyParameter chainCode) {
        super(new Ed25519PublicKey(privKeyParams.generatePublicKey()));
        this.privKeyParams = privKeyParams;
        this.chainCode = chainCode;
    }

    private Ed25519PrivateKey(Ed25519PrivateKeyParameters privKeyParams, Ed25519PublicKeyParameters pubKeyParams) {
        super(new Ed25519PublicKey(pubKeyParams));
        this.privKeyParams = privKeyParams;
        this.chainCode = null;
    }

    private static Ed25519PrivateKey derivableKey(byte[] deriveData) {
        final Ed25519PrivateKeyParameters privateKeyParameters = new Ed25519PrivateKeyParameters(deriveData, 0);
        final KeyParameter chainCode = new KeyParameter(deriveData, 32, 32);
        return new Ed25519PrivateKey(privateKeyParameters, chainCode);
    }

    private static Ed25519PrivateKey fromPrivateKeyInfo(PrivateKeyInfo privateKeyInfo) {
        Ed25519PrivateKeyParameters privKeyParams;
        Ed25519PublicKeyParameters pubKeyParams = null;

        try {
            ASN1Encodable privateKey = privateKeyInfo.parsePrivateKey();
            privKeyParams = new Ed25519PrivateKeyParameters(((ASN1OctetString) privateKey).getOctets(), 0);

            ASN1BitString pubKeyData = privateKeyInfo.getPublicKeyData();

            if (pubKeyData != null) {
                pubKeyParams = new Ed25519PublicKeyParameters(pubKeyData.getOctets(), 0);
            }

        } catch (IOException e) {
            throw new BadKeyException(e);
        }

        if (pubKeyParams != null) {
            return new Ed25519PrivateKey(privKeyParams, pubKeyParams);
        } else {
            return new Ed25519PrivateKey(privKeyParams);
        }
    }

    /**
     * Construct an Ed25519PrivateKey from a raw byte array.
     *
     * @throws BadKeyException if the key bytes are of an incorrect length for a raw
     *                         private key or private key + public key, or do not represent a DER encoded Ed25519
     *                         private key.
     */
    public static Ed25519PrivateKey fromBytes(byte[] keyBytes) {
        if (keyBytes.length == Ed25519.SECRET_KEY_SIZE) {
            // if the decoded bytes matches the length of a private key, try that
            return new Ed25519PrivateKey(new Ed25519PrivateKeyParameters(keyBytes, 0));
        } else if (keyBytes.length == Ed25519.SECRET_KEY_SIZE + Ed25519.PUBLIC_KEY_SIZE) {
            // some legacy code delivers raw private and public key pairs concatted together
            return new Ed25519PrivateKey(
                // this is how we read only the first 32 bytes
                new Ed25519PrivateKeyParameters(keyBytes, 0),
                // read the remaining 32 bytes as the public key
                new Ed25519PublicKeyParameters(keyBytes, Ed25519.SECRET_KEY_SIZE));
        } else {
            // decode a properly DER-encoded private key descriptor
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(keyBytes);
            return fromPrivateKeyInfo(privateKeyInfo);
        }
    }

    /**
     * Recover a private key from a generated mnemonic phrase and a passphrase.
     * <p>
     * This is not compatible with the phrases generated by the Android and iOS wallets;
     * use the no-passphrase version instead.
     *
     * @param mnemonic   the mnemonic phrase which should be a 24 byte list of words.
     * @param passphrase the passphrase used to protect the mnemonic (not used in the
     *                   mobile wallets, use {@link #fromMnemonic(Mnemonic)} instead.)
     * @return the recovered key; use {@link #derive(int)} to get a key for an account index (0
     * for default account)
     */
    public static Ed25519PrivateKey fromMnemonic(Mnemonic mnemonic, String passphrase) {
        final byte[] seed = mnemonic.toSeed(passphrase);

        final HMac hmacSha512 = new HMac(new SHA512Digest());
        hmacSha512.init(new KeyParameter("ed25519 seed".getBytes(StandardCharsets.UTF_8)));
        hmacSha512.update(seed, 0, seed.length);

        final byte[] derivedState = new byte[hmacSha512.getMacSize()];
        hmacSha512.doFinal(derivedState, 0);

        Ed25519PrivateKey derivedKey = derivableKey(derivedState);

        // BIP-44 path with the Hedera Hbar coin-type (omitting key index)
        // we pre-derive most of the path as the mobile wallets don't expose more than the index
        // https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki
        // https://github.com/satoshilabs/slips/blob/master/slip-0044.md
        for (int index : new int[]{44, 3030, 0, 0}) {
            derivedKey = derivedKey.derive(index);
        }

        return derivedKey;
    }

    /**
     * Recover a private key from a mnemonic phrase compatible with the iOS and Android wallets.
     * <p>
     * An overload of {@link #fromMnemonic(Mnemonic, String)} which uses an empty string for the
     * passphrase.
     *
     * @param mnemonic the mnemonic phrase which should be a 24 byte list of words.
     * @return the recovered key; use {@link #derive(int)} to get a key for an account index (0
     * for default account)
     */
    public static Ed25519PrivateKey fromMnemonic(Mnemonic mnemonic) {
        return fromMnemonic(mnemonic, "");
    }

    /**
     * Check if this private key supports derivation.
     * <p>
     * This is currently only the case if this private key was created from a mnemonic.
     */
    public boolean supportsDerivation() {
        return this.chainCode != null;
    }

    /**
     * Given a wallet/account index, derive a child key compatible with the iOS and Android wallets.
     * <p>
     * Use index 0 for the default account.
     *
     * @param index the wallet/account index of the account, 0 for the default account.
     * @return the derived key
     * @throws IllegalStateException if this key does not support derivation.
     * @see #supportsDerivation()
     */
    public Ed25519PrivateKey derive(int index) {
        if (this.chainCode == null) {
            throw new IllegalStateException("this private key does not support derivation");
        }

        // SLIP-10 child key derivation
        // https://github.com/satoshilabs/slips/blob/master/slip-0010.md#master-key-generation
        final HMac hmacSha512 = new HMac(new SHA512Digest());

        hmacSha512.init(chainCode);
        hmacSha512.update((byte) 0);

        hmacSha512.update(privKeyParams.getEncoded(), 0, Ed25519.SECRET_KEY_SIZE);

        // write the index in big-endian order, setting the 31st bit to mark it "hardened"
        final byte[] indexBytes = new byte[4];
        ByteBuffer.wrap(indexBytes).order(ByteOrder.BIG_ENDIAN).putInt(index);
        indexBytes[0] |= (byte) 0b10000000;

        hmacSha512.update(indexBytes, 0, indexBytes.length);

        byte[] output = new byte[64];
        hmacSha512.doFinal(output, 0);

        final Ed25519PrivateKeyParameters childKeyParams = new Ed25519PrivateKeyParameters(output, 0);
        final KeyParameter childChainCode = new KeyParameter(output, 32, 32);

        return new Ed25519PrivateKey(childKeyParams, childChainCode);
    }

    /**
     * Parse a private key from a PEM encoded reader.
     * <p>
     * This will read the first "PRIVATE KEY" section in the stream as an Ed25519 private key.
     *
     * @throws IOException if one occurred while reading or if no "PRIVATE KEY" section was found
     * @deprecated renamed to {@link #readPem(Reader)}.
     */
    @Deprecated
    public static Ed25519PrivateKey fromPem(Reader pemFile) throws IOException {
        return readPem(pemFile);
    }

    /**
     * Parse a private key from a PEM encoded reader.
     * <p>
     * This will read the first "PRIVATE KEY" section in the stream as an Ed25519 private key.
     *
     * @throws IOException     if one occurred while reading.
     * @throws BadKeyException if no "PRIVATE KEY" section was found or the key was not an Ed25519
     *                         private key.
     */
    public static Ed25519PrivateKey readPem(Reader pemFile) throws IOException {
        return readPem(pemFile, null);
    }

    /**
     * Parse a private key from a PEM encoded stream. The key may be encrypted, e.g. if it was
     * generated by OpenSSL.
     * <p>
     * If <i>password</i> is not null or empty, this will read the first "ENCRYPTED PRIVATE KEY"
     * section in the stream as a PKCS#8
     * <a href="https://tools.ietf.org/html/rfc5208#page-4">EncryptedPrivateKeyInfo</a> structure
     * and use that algorithm to decrypt the private key with the given password. Otherwise,
     * it will read the first "PRIVATE KEY" section as DER-encoded Ed25519 private key.
     * <p>
     * To generate an encrypted private key with OpenSSL, open a terminal and enter the following
     * command:
     * <pre>
     * openssl genpkey -algorithm ed25519 -aes-128-cbc > key.pem
     * </pre>
     * <p>
     * Then enter your password of choice when prompted. When the command completes, your encrypted
     * key will be saved as `key.pem` in the working directory of your terminal.
     *
     * @param pemFile  the PEM encoded file
     * @param password the password to decrypt the PEM file; if null or empty, no decryption is performed.
     * @throws IOException     if one occurred while reading the PEM file
     * @throws BadKeyException if no "ENCRYPTED PRIVATE KEY" or "PRIVATE KEY" section was found,
     *                         if the passphrase is wrong or the key was not an Ed25519 private key.
     */
    public static Ed25519PrivateKey readPem(Reader pemFile, @Nullable String password) throws IOException {
        return fromPrivateKeyInfo(PemUtils.readPrivateKey(pemFile, password));
    }

    /**
     * Parse a private key from a PEM encoded string.
     *
     * @throws IOException     if the PEM string was improperly encoded
     * @throws BadKeyException if no "PRIVATE KEY" section was found or the key was not an Ed25519
     *                         private key.
     * @see #readPem(Reader)
     */
    public static Ed25519PrivateKey fromPem(String pemEncoded) throws IOException {
        return readPem(new StringReader(pemEncoded));
    }

    /**
     * Parse a private key from a PEM encoded string.
     * <p>
     * The private key may be encrypted, e.g. if it was generated by OpenSSL.
     *
     * @param encodedPem the encoded PEM string
     * @param password   the password to decrypt the PEM file; if null or empty, no decryption is performed.
     * @throws IOException     if the PEM string was improperly encoded
     * @throws BadKeyException if no "ENCRYPTED PRIVATE KEY" or "PRIVATE KEY" section was found,
     *                         if the passphrase is wrong or the key was not an Ed25519 private key.
     * @see #readPem(Reader, String)
     */
    public static Ed25519PrivateKey fromPem(String encodedPem, @Nullable String password) throws IOException {
        return readPem(new StringReader(encodedPem), password);
    }

    /**
     * Recover a private key from its text-encoded representation.
     *
     * @param privateKeyString the hex-encoded private key string
     * @return the restored private key
     * @throws org.bouncycastle.util.encoders.DecoderException if the hex string is invalid
     * @throws RuntimeException                                if the decoded key was invalid
     */
    public static Ed25519PrivateKey fromString(String privateKeyString) {
        // TODO: catch unchecked `DecoderException`
        byte[] keyBytes = Hex.decode(privateKeyString);
        return fromBytes(keyBytes);
    }

    /**
     * Recover a private key from an encrypted keystore file.
     *
     * @param inputStream the inputstream to read the keystore from
     * @param passphrase  the passphrase used to encrypt the keystore
     * @return the recovered key
     * @throws IOException     if any occurs while reading from the inputstream
     * @throws BadKeyException if there is a problem with the keystore; most likely
     *                         is if the passphrase is incorrect.
     */
    public static Ed25519PrivateKey readKeystore(InputStream inputStream, String passphrase) throws IOException {
        return Keystore.fromStream(inputStream, passphrase).getEd25519();
    }

    /**
     * Recover a private key from an encrypted keystore encoded as a byte array.
     *
     * @param bytes      the binary-encoded keystore
     * @param passphrase the passphrase used to encrypt the keystore
     * @return the recovered key
     * @throws BadKeyException if there is a problem with the keystore; most likely
     *                         is if the passphrase is incorrect.
     */
    public static Ed25519PrivateKey fromKeystore(byte[] bytes, String passphrase) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        try {
            return readKeystore(bis, passphrase);
        } catch (IOException e) {
            // IO exceptions shouldn't be thrown by this variant but might happen for bad JSON
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate a new, random private key which supports child key derivation.
     *
     * @return a new private key using {@link java.security.SecureRandom}.
     */
    public static Ed25519PrivateKey generate() {
        return generate(CryptoUtils.secureRandom);
    }

    /**
     * Generate a new, random private key which supports child key derivation.
     *
     * @param secureRandom the source of randomness to use.
     * @return a new, random private key.
     */
    public static Ed25519PrivateKey generate(SecureRandom secureRandom) {
        // extra 32 bytes for chain code
        byte[] deriveData = new byte[64];
        secureRandom.nextBytes(deriveData);
        return Ed25519PrivateKey.derivableKey(deriveData);
    }

    @Override
    public byte[] toBytes() {
        return privKeyParams.getEncoded();
    }

    @Override
    public byte[] sign(byte[] message, int messageOffset, int messageLen) {
        byte[] secret = privKeyParams.getEncoded();
        byte[] sigBytes = new byte[Ed25519.SIGNATURE_SIZE];
        Ed25519.sign(secret, 0, message, messageOffset, messageLen, sigBytes, 0);

        return sigBytes;
    }

    private byte[] encodeDER() {
        PrivateKeyInfo privateKeyInfo = toPrivateKeyInfo();

        try {
            return privateKeyInfo.getEncoded("DER");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PrivateKeyInfo toPrivateKeyInfo() {
        try {
            return new PrivateKeyInfo(
                new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519),
                new DEROctetString(privKeyParams.getEncoded()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return Hex.toHexString(encodeDER());
    }

    /**
     * Write out a PEM encoded version of this private key.
     *
     * @deprecated for removal; exporting unencrypted PEMs is very insecure and has dubious
     * utility.
     */
    @Deprecated
    public void writePem(Writer out) throws IOException {
        final PemWriter pemWriter = new PemWriter(out);
        pemWriter.writeObject(new PemObject(PemUtils.TYPE_PRIVATE_KEY, encodeDER()));
        pemWriter.flush();
    }

    /**
     * Export a keystore file to the given {@link OutputStream} encrypted with a given passphrase.
     * <p>
     * You can recover this key later with {@link #readKeystore(InputStream, String)}.
     *
     * @param outputStream the OutputStream to write the keystore to.
     * @param passphrase   the passphrase to encrypt the keystore with.
     * @throws IOException if any occurs while writing to the OutputStream
     */
    public void writeKeystore(OutputStream outputStream, String passphrase) throws IOException {
        new Keystore(this).export(outputStream, passphrase);
    }

    /**
     * Export a keystore file to a byte array encrypted with a given passphrase.
     * <p>
     * You can recover this key later with {@link #fromKeystore(byte[], String)}.
     *
     * @param passphrase the passphrase to encrypt the keystore with.
     */
    public byte[] toKeystore(String passphrase) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            writeKeystore(bos, passphrase);
        } catch (IOException e) {
            // any IOException here is a problem in the JSON encoding
            throw new RuntimeException(e);
        }

        return bos.toByteArray();
    }
}
