package com.hedera.hashgraph.sdk;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;

/**
 * A common base for the signing authority or key that entities in Hedera may have.
 *
 * @see KeyList
 * @see PublicKey
 */
public abstract class Key {
    static final ASN1ObjectIdentifier ID_ED25519 = new ASN1ObjectIdentifier("1.3.101.112");

    static Key fromProtobuf(com.hedera.hashgraph.sdk.proto.Key key) {
        switch (key.getKeyCase()) {
            case ED25519:
                return new PublicKey(key.getEd25519().toByteArray());

            case KEYLIST:
                return KeyList.fromProtobuf(key.getKeyList(), null);

            case THRESHOLDKEY:
                return KeyList.fromProtobuf(key.getThresholdKey().getKeys(), key.getThresholdKey().getThreshold());

            default:
                throw new IllegalStateException("Key#fromProtobuf: unhandled key case: " + key.getKeyCase());
        }
    }

    /**
     * Serialize this key as a protobuf object
     */
    abstract com.hedera.hashgraph.sdk.proto.Key toKeyProtobuf();

    public byte[] toBytes() {
        return toKeyProtobuf().toByteArray();
    }
}
