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

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.NftID;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * The (non-fungible) token of which this NFT is an instance
 */
public class NftId implements Comparable<NftId> {
    /**
     * The (non-fungible) token of which this NFT is an instance
     */
    public final TokenId tokenId;

    /**
     * The unique identifier of this instance
     */
    @Nonnegative
    public final long serial;

    /**
     * Constructor.
     *
     * @param tokenId                   the token id
     * @param serial                    the serial number
     */
    public NftId(TokenId tokenId, @Nonnegative long serial) {
        this.tokenId = Objects.requireNonNull(tokenId);
        this.serial = serial;
    }

    /**
     * Create a new nft id from a string.
     *
     * @param id                        the string representation
     * @return                          the new nft id
     */
    public static NftId fromString(String id) {
        @SuppressWarnings("StringSplitter")
        var parts = id.split("[/@]");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Expecting {shardNum}.{realmNum}.{idNum}-{checksum}/{serialNum}");
        }
        return new NftId(TokenId.fromString(parts[0]), Long.parseLong(parts[1]));
    }

    /**
     * Create a new ntf id from a protobuf.
     *
     * @param nftId                     the protobuf representation
     * @return                          the new nft id
     */
    static NftId fromProtobuf(NftID nftId) {
        Objects.requireNonNull(nftId);
        var tokenId = nftId.getTokenID();
        return new NftId(TokenId.fromProtobuf(tokenId), nftId.getSerialNumber());
    }

    /**
     * Create a new nft id from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new nft id
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static NftId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(NftID.parseFrom(bytes).toBuilder().build());
    }

    /**
     * Create the protobuf.
     *
     * @return                          a protobuf representation
     */
    NftID toProtobuf() {
        return NftID.newBuilder()
            .setTokenID(tokenId.toProtobuf())
            .setSerialNumber(serial)
            .build();
    }

    /**
     * Create the byte array.
     *
     * @return                          a byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return tokenId.toString() + "/" + serial;
    }

    /**
     * Generate a string representation with checksum.
     *
     * @param client                    the configured client
     * @return                          the string representation with checksum
     */
    public String toStringWithChecksum(Client client) {
        return tokenId.toStringWithChecksum(client) + "/" + serial;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenId.shard, tokenId.realm, tokenId.num, serial);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof NftId)) {
            return false;
        }

        NftId otherId = (NftId) o;
        return tokenId.equals(otherId.tokenId) && serial == otherId.serial;
    }

    @Override
    public int compareTo(NftId o) {
        Objects.requireNonNull(o);
        int tokenComparison = tokenId.compareTo(o.tokenId);
        if (tokenComparison != 0) {
            return tokenComparison;
        }
        return Long.compare(serial, o.serial);
    }
}
