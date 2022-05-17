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

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.AccountID;
import org.bouncycastle.util.encoders.Hex;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The ID for a crypto-currency account on Hedera.
 */
public final class EvmAddress extends Key {
    private final byte[] bytes;

    public EvmAddress(byte[] bytes) {
        this.bytes = bytes;
    }

    public static EvmAddress fromString(String text) {
        return new EvmAddress(Hex.decode(text));
    }

    @Nullable
    static EvmAddress fromAliasBytes(ByteString aliasBytes) {
        if (!aliasBytes.isEmpty()) {
            try {
                var key = Key.fromProtobufKey(com.hedera.hashgraph.sdk.proto.Key.parseFrom(aliasBytes));
                return (key instanceof EvmAddress) ? ((EvmAddress) key) : null;
            } catch (InvalidProtocolBufferException ignored) {
            }
        }
        return null;
    }

    public static EvmAddress fromBytes(byte[] bytes) {
        return new EvmAddress(bytes);
    }

    com.hedera.hashgraph.sdk.proto.Key toProtobufKey() {
        return com.hedera.hashgraph.sdk.proto.Key.newBuilder()
            .setEd25519(ByteString.copyFrom(bytes))
            .build();
    }

    public byte[] toBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return Hex.toHexString(bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof EvmAddress)) {
            return false;
        }

        EvmAddress other = (EvmAddress) o;
        return Arrays.equals(bytes, other.bytes);
    }
}
