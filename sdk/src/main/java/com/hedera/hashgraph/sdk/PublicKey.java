package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

/**
 * A public key on the Hedera™ network.
 */
public abstract class PublicKey extends Key {
    /**
     * Create a public key from a byte array.
     *
     * @param publicKey                 the byte array
     * @return                          the new public key
     */
    public static PublicKey fromBytes(byte[] publicKey) {
        if (publicKey.length == Ed25519.PUBLIC_KEY_SIZE) {
            // If this is a 32 byte string, assume an Ed25519 public key
            return new PublicKeyED25519(publicKey);
        }

        // Assume a DER-encoded private key descriptor
        return fromBytesDER(publicKey);
    }

    /**
     * Create a public key from a DER encoded byte array.
     *
     * @param publicKey                 the DER encoded byte array
     * @return                          the new key
     */
    public static PublicKey fromBytesDER(byte[] publicKey) {
        return PublicKey.fromSubjectKeyInfo(SubjectPublicKeyInfo.getInstance(publicKey));
    }

    /**
     * Create a public key from a byte array.
     *
     * @param publicKey                 the byte array
     * @return                          the new key
     */
    public static PublicKey fromBytesED25519(byte[] publicKey) {
        return PublicKeyED25519.fromBytesInternal(publicKey);
    }

    /**
     * Create a public key from a byte array.
     *
     * @param publicKey                 the byte array
     * @return                          the new key
     */
    public static PublicKey fromBytesECDSA(byte[] publicKey) {
        return PublicKeyECDSA.fromBytesInternal(publicKey);
    }

    /**
     * Create a public key from a string.
     *
     * @param publicKey                 the string
     * @return                          the new key
     */
    public static PublicKey fromString(String publicKey) {
        return PublicKey.fromBytes(Hex.decode(publicKey));
    }

    /**
     * Create a public key from a string.
     *
     * @param publicKey                 the string
     * @return                          the new key
     */
    public static PublicKey fromStringED25519(String publicKey) {
        return fromBytesED25519(Hex.decode(publicKey));
    }

    /**
     * Create a public key from a string.
     *
     * @param publicKey                 the string
     * @return                          the new key
     */
    public static PublicKey fromStringECDSA(String publicKey) {
        return fromBytesECDSA(Hex.decode(publicKey));
    }

    /**
     * Create a public key from a string.
     *
     * @param publicKey                 the string
     * @return                          the new key
     */
    public static PublicKey fromStringDER(String publicKey) {
        return fromBytesDER(Hex.decode(publicKey));
    }

    /**
     * Create a public key from a subject public key info object.
     *
     * @param subjectPublicKeyInfo      the subject public key info object
     * @return                          the new key
     */
    private static PublicKey fromSubjectKeyInfo(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        if(subjectPublicKeyInfo.getAlgorithm().equals(new AlgorithmIdentifier(ID_ED25519))) {
            return PublicKeyED25519.fromSubjectKeyInfoInternal(subjectPublicKeyInfo);
        } else {
            // assume ECDSA
            return PublicKeyECDSA.fromSubjectKeyInfoInternal(subjectPublicKeyInfo);
        }
    }

    /**
     * The public key from an immutable byte string.
     *
     * @param aliasBytes                the immutable byte string
     * @return                          the key
     */
    @Nullable
    static PublicKey fromAliasBytes(ByteString aliasBytes) {
        if (!aliasBytes.isEmpty()) {
            try {
                var key = Key.fromProtobufKey(com.hedera.hashgraph.sdk.proto.Key.parseFrom(aliasBytes));
                return (key instanceof PublicKey) ? ((PublicKey) key) : null;
            } catch (InvalidProtocolBufferException ignored) {
            }
        }
        return null;
    }

    /**
     * Verify a signature on a message with this public key.
     *
     * @param message   The array of bytes representing the message
     * @param signature The array of bytes representing the signature
     * @return boolean
     */
    public abstract boolean verify(byte[] message, byte[] signature);

    /**
     * Get the signature from a signature pair protobuf.
     *
     * @param pair                      the protobuf
     * @return                          the signature
     */
    abstract ByteString extractSignatureFromProtobuf(SignaturePair pair);

    /**
     * Is the given transaction valid?
     *
     * @param transaction               the transaction
     * @return                          is it valid
     */
    public boolean verifyTransaction(Transaction<?> transaction) {
        if (!transaction.isFrozen()) {
            transaction.freeze();
        }

        for (var publicKey : transaction.publicKeys) {
            if (publicKey.equals(this)) {
                return true;
            }
        }

        for (var signedTransaction : transaction.innerSignedTransactions) {
            @Var var found = false;

            for (var sigPair : signedTransaction.getSigMap().getSigPairList()) {
                if (sigPair.getPubKeyPrefix().equals(ByteString.copyFrom(toBytesRaw()))) {
                    found = true;

                    if (!verify(signedTransaction.getBodyBytes().toByteArray(), extractSignatureFromProtobuf(sigPair).toByteArray())) {
                        return false;
                    }
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }

    /**
     * Serialize this key as a SignaturePair protobuf object
     */
    abstract SignaturePair toSignaturePairProtobuf(byte[] signature);

    @Override
    public abstract byte[] toBytes();

    /**
     * @return                          the DER represented as a byte array
     */
    public abstract byte[] toBytesDER();

    /**
     * @return                          the raw byte representation
     */
    public abstract byte[] toBytesRaw();

    @Override
    public String toString() {
        return toStringDER();
    }

    /**
     * @return                          the DER encoded string
     */
    public String toStringDER() {
        return Hex.toHexString(toBytesDER());
    }

    /**
     * @return                          the raw string
     */
    public String toStringRaw() {
        return Hex.toHexString(toBytesRaw());
    }

    /**
     * Create a new account id.
     *
     * @param shard                     the shard part
     * @param realm                     the realm part
     * @return                          the new account id
     */
    public AccountId toAccountId(@Nonnegative long shard, @Nonnegative long realm) {
        return new AccountId(shard, realm, 0, null, this);
    }

    /**
     * @return                          is this an ED25519 key
     */
    public abstract boolean isED25519();

    /**
     * @return                          is this an ECDSA key
     */
    public abstract boolean isECDSA();
}
