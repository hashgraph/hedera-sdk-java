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
    public static PublicKey fromBytes(byte[] publicKey) {
        if (publicKey.length == Ed25519.PUBLIC_KEY_SIZE) {
            // If this is a 32 byte string, assume an Ed25519 public key
            return new PublicKeyED25519(publicKey);
        }

        // Assume a DER-encoded private key descriptor
        return fromBytesDER(publicKey);
    }

    public static PublicKey fromBytesDER(byte[] publicKey) {
        return PublicKey.fromSubjectKeyInfo(SubjectPublicKeyInfo.getInstance(publicKey));
    }

    public static PublicKey fromBytesED25519(byte[] publicKey) {
        return PublicKeyED25519.fromBytesInternal(publicKey);
    }

    public static PublicKey fromBytesECDSA(byte[] publicKey) {
        return PublicKeyECDSA.fromBytesInternal(publicKey);
    }

    public static PublicKey fromString(String publicKey) {
        return PublicKey.fromBytes(Hex.decode(publicKey));
    }

    public static PublicKey fromStringED25519(String publicKey) {
        return fromBytesED25519(Hex.decode(publicKey));
    }

    public static PublicKey fromStringECDSA(String publicKey) {
        return fromBytesECDSA(Hex.decode(publicKey));
    }

    public static PublicKey fromStringDER(String publicKey) {
        return fromBytesDER(Hex.decode(publicKey));
    }

    private static PublicKey fromSubjectKeyInfo(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        if(subjectPublicKeyInfo.getAlgorithm().equals(new AlgorithmIdentifier(ID_ED25519))) {
            return PublicKeyED25519.fromSubjectKeyInfoInternal(subjectPublicKeyInfo);
        } else {
            // assume ECDSA
            return PublicKeyECDSA.fromSubjectKeyInfoInternal(subjectPublicKeyInfo);
        }
    }

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

    abstract ByteString extractSignatureFromProtobuf(SignaturePair pair);

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

    public abstract byte[] toBytesDER();

    public abstract byte[] toBytesRaw();

    @Override
    public String toString() {
        return toStringDER();
    }

    public String toStringDER() {
        return Hex.toHexString(toBytesDER());
    }

    public String toStringRaw() {
        return Hex.toHexString(toBytesRaw());
    }

    public AccountId toAccountId(@Nonnegative long shard, @Nonnegative long realm) {
        return new AccountId(shard, realm, 0, null, this);
    }
}
