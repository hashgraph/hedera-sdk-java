/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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
package com.hiero.tck.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hiero.sdk.Key;
import com.hiero.sdk.PrivateKey;
import com.hiero.sdk.PublicKey;

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

        public static KeyType fromString(String keyString) {
            for (KeyType type : KeyType.values()) {
                if (type.keyString.equals(keyString)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown key type: " + keyString);
        }
    }

    public static Key getKeyFromString(String keyString) throws InvalidProtocolBufferException {
        try {
            return PublicKey.fromStringDER(keyString);
        } catch (Exception e) {
            try {
                return PrivateKey.fromStringDER(keyString);
            } catch (Exception ex) {
                return Key.fromBytes(ByteString.fromHex(keyString).toByteArray());
            }
        }
    }
}
