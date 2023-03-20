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
import com.hedera.hashgraph.sdk.proto.SignedTransaction;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

/**
 * A private key on the Hedera™ network.
 */
public abstract class PrivateKey extends Key {
    /**
     * The public key derived from the private key
     */
    @Nullable
    protected PublicKey publicKey = null; // Cache the derivation of the public key

    /**
     * Generates a new <a href="https://ed25519.cr.yp.to/">Ed25519</a> private key.
     *
     * @deprecated use {@link #generateED25519()} or {@link #generateECDSA()} instead
     *
     * @return the new Ed25519 private key.
     */
    public static PrivateKey generate() {
        return generateED25519();
    }

    /**
     * Extract the new ED25519 private key.
     *
     * @return                          the new ED25519 private key
     */
    public static PrivateKey generateED25519() {
        return PrivateKeyED25519.generateInternal();
    }

    /**
     * Extract the new ECDSA private key.
     *
     * @return                          the new ECDSA private key
     */
    public static PrivateKey generateECDSA() {
        return PrivateKeyECDSA.generateInternal();
    }

    /**
     * Extract the ED25519 private key from a seed.
     *
     * @param seed  the seed
     * @return      the ED25519 private key
     */
    public static PrivateKey fromSeedED25519(byte[] seed) {
        return PrivateKeyED25519.fromSeed(seed);
    }

    /**
     * Extract the ECDSA private key from a seed.
     *
     * @param seed  the seed
     * @return      the ECDSA private key
     */
    public static PrivateKey fromSeedECDSAsecp256k1(byte[] seed) {
        return PrivateKeyECDSA.fromSeed(seed);
    }

    /**
     * @deprecated use {@link Mnemonic#toStandardEd25519PrivateKey(String, int)} ()} or {@link Mnemonic#toStandardECDSAsecp256k1PrivateKey(String, int)} (String, int)} instead
     * This function uses incomplete and the key should not be used directly.
     * <p>
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
    @Deprecated
    public static PrivateKey fromMnemonic(Mnemonic mnemonic, String passphrase) {
        var seed = mnemonic.toSeed(passphrase);
        @Var PrivateKey derivedKey = fromSeedED25519(seed);

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
     * @deprecated use {@link Mnemonic#toStandardEd25519PrivateKey(String, int)} ()} or {@link Mnemonic#toStandardECDSAsecp256k1PrivateKey(String, int)} (String, int)} instead
     * Recover a private key from a mnemonic phrase compatible with the iOS and Android wallets.
     * <p>
     * An overload of {@link #fromMnemonic(Mnemonic, String)} which uses an empty string for the
     * passphrase.
     *
     * @param mnemonic the mnemonic phrase which should be a 24 byte list of words.
     * @return the recovered key; use {@link #derive(int)} to get a key for an account index (0
     * for default account)
     */
    @Deprecated
    public static PrivateKey fromMnemonic(Mnemonic mnemonic) {
        return fromMnemonic(mnemonic, "");
    }

    /**
     * Retrieve a private key from a string.
     *
     * @param privateKey                string representing a private key
     * @return                          the private key
     */
    public static PrivateKey fromString(String privateKey) {
        return fromBytes(Hex.decode(privateKey.startsWith("0x") ? privateKey.substring(2) : privateKey));
    }

    /**
     * Retrieve a private key from a DER encoded string.
     *
     * @param privateKey                DER encoded string representing a private key
     * @return                          the private key
     */
    public static PrivateKey fromStringDER(String privateKey) {
        return fromBytesDER(Hex.decode(privateKey));
    }

    /**
     * Retrieve a private key from an ED25519 encoded string.
     *
     * @param privateKey                ED25519 encoded string representing a private key
     * @return                          the private key
     */
    public static PrivateKey fromStringED25519(String privateKey) {
        return fromBytesED25519(Hex.decode(privateKey));
    }

    /**
     * Retrieve a private key from an ECDSA encoded string.
     *
     * @param privateKey                ECDSA encoded string representing a private key
     * @return                          the private key
     */
    public static PrivateKey fromStringECDSA(String privateKey) {
        return fromBytesECDSA(Hex.decode(privateKey));
    }

    /**
     * Retrieve a private key from a byte array.
     *
     * @param privateKey                byte array representing a private key
     * @return                          the private key
     */
    public static PrivateKey fromBytes(byte[] privateKey) {
        if ((privateKey.length == Ed25519.SECRET_KEY_SIZE)
            || (privateKey.length == Ed25519.SECRET_KEY_SIZE + Ed25519.PUBLIC_KEY_SIZE)) {
            // If this is a 32 or 64 byte string, assume an Ed25519 private key
            return new PrivateKeyED25519(Arrays.copyOfRange(privateKey, 0, Ed25519.SECRET_KEY_SIZE), null);
        }

        // Assume a DER-encoded private key descriptor
        return fromBytesDER(privateKey);
    }

    /**
     * Retrieve a private key from an ED25519 encoded byte array.
     *
     * @param privateKey                ED25519 encoded byte array representing a private key
     * @return                          the private key
     */
    public static PrivateKey fromBytesED25519(byte[] privateKey) {
        return PrivateKeyED25519.fromBytesInternal(privateKey);
    }

    /**
     * Retrieve a private key from an ECDSA encoded byte array.
     *
     * @param privateKey                ECDSA encoded byte array representing a private key
     * @return                          the private key
     */
    public static PrivateKey fromBytesECDSA(byte[] privateKey) {
        return PrivateKeyECDSA.fromBytesInternal(privateKey);
    }

    /**
     * Retrieve a private key from a DER encoded byte array.
     *
     * @param privateKey                DER encoded byte array representing a private key
     * @return                          the private key
     */
    public static PrivateKey fromBytesDER(byte[] privateKey) {
        return PrivateKey.fromPrivateKeyInfo(PrivateKeyInfo.getInstance(privateKey));
    }

    /**
     * Retrieve a private key from a private key info object.
     *
     * @param privateKeyInfo            private key info object
     * @return                          the private key
     */
    private static PrivateKey fromPrivateKeyInfo(PrivateKeyInfo privateKeyInfo) {
        if (privateKeyInfo.getPrivateKeyAlgorithm().equals(new AlgorithmIdentifier(ID_ED25519))) {
            return PrivateKeyED25519.fromPrivateKeyInfoInternal(privateKeyInfo);
        } else {
            // assume ECDSA
            return PrivateKeyECDSA.fromPrivateKeyInfoInternal(privateKeyInfo);
        }
    }

    /**
     * Parse a private key from a PEM encoded reader.
     * <p>
     * This will read the first "PRIVATE KEY" section in the stream as an Ed25519 private key.
     *
     * @param pemFile The Reader containing the pem file
     * @return {@link com.hedera.hashgraph.sdk.PrivateKey}
     * @throws IOException     if one occurred while reading.
     * @throws BadKeyException if no "PRIVATE KEY" section was found or the key was not an Ed25519
     *                         private key.
     */
    public static PrivateKey readPem(Reader pemFile) throws IOException {
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
     * {@code openssl genpkey -algorithm ed25519 -aes-128-cbc > key.pem}
     * </pre>
     * <p>
     * Then enter your password of choice when prompted. When the command completes, your encrypted
     * key will be saved as `key.pem` in the working directory of your terminal.
     *
     * @param pemFile  the PEM encoded file
     * @param password the password to decrypt the PEM file; if null or empty, no decryption is performed.
     * @return {@link com.hedera.hashgraph.sdk.PrivateKey}
     * @throws IOException     if one occurred while reading the PEM file
     * @throws BadKeyException if no "ENCRYPTED PRIVATE KEY" or "PRIVATE KEY" section was found,
     *                         if the passphrase is wrong or the key was not an Ed25519 private key.
     */
    public static PrivateKey readPem(Reader pemFile, @Nullable String password) throws IOException {
        return fromPrivateKeyInfo(Pem.readPrivateKey(pemFile, password));
    }

    /**
     * Parse a private key from a PEM encoded string.
     *
     * @param pemEncoded The String containing the pem
     * @return {@link com.hedera.hashgraph.sdk.PrivateKey}
     * @throws IOException     if the PEM string was improperly encoded
     * @throws BadKeyException if no "PRIVATE KEY" section was found or the key was not an Ed25519
     *                         private key.
     * @see #readPem(Reader)
     */
    public static PrivateKey fromPem(String pemEncoded) throws IOException {
        return readPem(new StringReader(pemEncoded));
    }

    /**
     * Parse a private key from a PEM encoded string.
     * <p>
     * The private key may be encrypted, e.g. if it was generated by OpenSSL.
     *
     * @param encodedPem the encoded PEM string
     * @param password   the password to decrypt the PEM file; if null or empty, no decryption is performed.
     * @return {@link com.hedera.hashgraph.sdk.PrivateKey}
     * @throws IOException     if the PEM string was improperly encoded
     * @throws BadKeyException if no "ENCRYPTED PRIVATE KEY" or "PRIVATE KEY" section was found,
     *                         if the passphrase is wrong or the key was not an Ed25519 private key.
     * @see #readPem(Reader, String)
     */
    public static PrivateKey fromPem(String encodedPem, @Nullable String password) throws IOException {
        return readPem(new StringReader(encodedPem), password);
    }

    /**
     * Derive a child key based on the index.
     *
     * @param index                     the index
     * @return                          the derived child key
     */
    public PrivateKey legacyDerive(int index) {
        return legacyDerive((long) index);
    }

    /**
     * Derive a child key based on the index.
     *
     * @param index                     the index
     * @return                          the derived child key
     */
    public abstract PrivateKey legacyDerive(long index);

    /**
     * Check if this private key supports derivation.
     * <p>
     * This is currently only the case if this private key was created from a mnemonic.
     *
     * @return boolean
     */
    public abstract boolean isDerivable();

    /**
     * Given a wallet/account index, derive a child key compatible with the iOS and Android wallets.
     * <p>
     * Use index 0 for the default account.
     *
     * @param index the wallet/account index of the account, 0 for the default account.
     * @return the derived key
     * @throws IllegalStateException if this key does not support derivation.
     * @see #isDerivable()
     */
    public abstract PrivateKey derive(int index);

    /**
     * Derive a public key from this private key.
     *
     * <p>The public key can be freely given and used by other parties to verify the signatures
     * generated by this private key.
     *
     * @return the corresponding public key for this private key.
     */
    public abstract PublicKey getPublicKey();

    /**
     * Sign a message with this private key.
     *
     * @param message The array of bytes to sign with
     * @return the signature of the message.
     */
    public abstract byte[] sign(byte[] message);

    /**
     * Sign a transaction.
     *
     * @param transaction               the transaction
     * @return                          the signed transaction
     */
    public byte[] signTransaction(Transaction<?> transaction) {
        transaction.requireOneNodeAccountId();

        if (!transaction.isFrozen()) {
            transaction.freeze();
        }

        var builder = (SignedTransaction.Builder) transaction.innerSignedTransactions.get(0);
        var signature = sign(builder.getBodyBytes().toByteArray());

        transaction.addSignature(getPublicKey(), signature);

        return signature;
    }

    @Override
    public abstract byte[] toBytes();

    /**
     * Extract the byte array encoded as DER.
     *
     * @return                          the byte array encoded as DER
     */
    public abstract byte[] toBytesDER();

    /**
     * Extract the raw byte array.
     *
     * @return                          the raw byte array
     */
    public abstract byte[] toBytesRaw();

    @Override
    public String toString() {
        return toStringDER();
    }

    /**
     * Extract the DER encoded hex string.
     *
     * @return                          the DER encoded hex string
     */
    public String toStringDER() {
        return Hex.toHexString(toBytesDER());
    }

    /**
     * Extract the raw hex string.
     *
     * @return                          the raw hex string
     */
    public String toStringRaw() {
        return Hex.toHexString(toBytesRaw());
    }

    /**
     * Retrieve the account id.
     *
     * @param shard                     the shard
     * @param realm                     the realm
     * @return                          the account id
     */
    public AccountId toAccountId(@Nonnegative long shard, @Nonnegative long realm) {
        return getPublicKey().toAccountId(shard, realm);
    }

    @Override
    com.hedera.hashgraph.sdk.proto.Key toProtobufKey() {
        // Forward to the corresponding public key.
        return getPublicKey().toProtobufKey();
    }

    /**
     * Are we an ED25519 key?
     *
     * @return                          are we an ED25519 key
     */
    public abstract boolean isED25519();

    /**
     * Are we an ECDSA key?
     *
     * @return                          are we an ECDSA key
     */
    public abstract boolean isECDSA();

    /**
     * Get the chain code of the key
     *
     * @return the chainCode
     */
    public abstract KeyParameter getChainCode();
}
