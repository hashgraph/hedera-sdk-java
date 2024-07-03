package com.hedera.hashgraph.tck.util;

import static com.hedera.hashgraph.sdk.Key.fromProtobufKey;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.Key;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import org.bouncycastle.util.encoders.Hex;

public final class KeyUtils {

    public enum KeyType {
        ED25519_PRIVATE_KEY("ed25519PrivateKey"),
        ED25519_PUBLIC_KEY("ed25519PublicKey"),
        ECDSA_SECP256K1_PRIVATE_KEY("ecdsaSecp256k1PrivateKey"),
        ECDSA_SECP256K1_PUBLIC_KEY("ecdsaSecp256k1PublicKey"),
        LIST_KEY("keyList"),
        THRESHOLD_KEY("thresholdKey"),
        EVM_ADDRESS_KEY("evmAddress");

        private final String keyString;

        KeyType(String keyString) {
            this.keyString = keyString;
        }

        public String getKeyString() {
            return keyString;
        }

        public static KeyType fromString(String keyString) {
            for (KeyType type : KeyType.values()) {
                if (type.keyString.equals(keyString)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown key type: " + keyString);
        }
    }

    // TODO: cover with test(s)
    public static Key getKeyFromStringDER(String keyStringDER) throws InvalidProtocolBufferException {
        try {
            return PublicKey.fromStringDER(keyStringDER);
        } catch (Exception e) {
            try {
                return PrivateKey.fromStringDER(keyStringDER);
            } catch (Exception ex) {
                com.hedera.hashgraph.sdk.proto.Key protoKey = com.hedera.hashgraph.sdk.proto.Key.getDefaultInstance();
                com.hedera.hashgraph.sdk.proto.Key something =
                        protoKey.getParserForType().parseFrom(Hex.decode(keyStringDER));
                return fromProtobufKey(something);
            }
        }
    }
}
