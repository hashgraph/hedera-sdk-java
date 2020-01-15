package com.hedera.hashgraph.sdk.crypto;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.ContractIDOrBuilder;
import com.hedera.hashgraph.proto.KeyOrBuilder;
import com.hedera.hashgraph.proto.SignaturePair;
import com.hedera.hashgraph.sdk.Internal;
import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.bouncycastle.util.encoders.Hex;

public abstract class PublicKey {
    @Internal
    public abstract com.hedera.hashgraph.proto.Key toKeyProto();

    @Internal
    public boolean hasPrefix(ByteString prefix) {
        return false;
    }

    @Internal
    public static PublicKey fromProtoKey(KeyOrBuilder key) {
        switch (key.getKeyCase()) {
        case ED25519:
            return Ed25519PublicKey.fromBytes(
                key.getEd25519()
                    .toByteArray());
        case CONTRACTID:
            ContractIDOrBuilder id = key.getContractIDOrBuilder();
            return new ContractId(id.getShardNum(), id.getRealmNum(), id.getContractNum());
        default:
            throw new IllegalStateException("Unchecked Key Case");
        }
    }

    public abstract byte[] toBytes();

    @Internal
    public abstract SignaturePair.SignatureCase getSignatureCase();

    public static PublicKey fromString(String keyString) {
        SubjectPublicKeyInfo pubKeyInfo;

        try {
            byte[] keyBytes = Hex.decode(keyString);

            // it could be a hex-encoded raw public key or a DER-encoded public key
            if (keyBytes.length == Ed25519.PUBLIC_KEY_SIZE) {
                return Ed25519PublicKey.fromBytes(keyBytes);
            }

            pubKeyInfo = SubjectPublicKeyInfo.getInstance(keyBytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse public key", e);
        }

        ASN1ObjectIdentifier algId = pubKeyInfo.getAlgorithm()
            .getAlgorithm();

        if (algId.equals(EdECObjectIdentifiers.id_Ed25519)) {
            return Ed25519PublicKey.fromBytes(
                pubKeyInfo.getPublicKeyData()
                    .getBytes());
        } else {
            throw new IllegalArgumentException("Unsupported public key type: " + algId.toString());
        }
    }
}
