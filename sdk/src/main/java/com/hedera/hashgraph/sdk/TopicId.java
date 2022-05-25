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
import com.hedera.hashgraph.sdk.proto.TopicID;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Unique identifier for a topic (used by the consensus service).
 */
public final class TopicId implements Comparable<TopicId> {
    /**
     * The shard number
     */
    @Nonnegative
    public final long shard;

    /**
     * The realm number
     */
    @Nonnegative
    public final long realm;

    /**
     * The id number
     */
    @Nonnegative
    public final long num;

    @Nullable
    private final String checksum;

    /**
     * Constructor.
     *
     * @param num                       the num part
     */
    public TopicId(@Nonnegative long num) {
        this(0, 0, num);
    }

    /**
     * Constructor.
     *
     * @param shard                     the shard part
     * @param realm                     the realm part
     * @param num                       the num part
     */
    @SuppressWarnings("InconsistentOverloads")
    public TopicId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this(shard, realm, num, null);
    }

    /**
     * Constructor.
     *
     * @param shard                     the shard part
     * @param realm                     the realm part
     * @param num                       the num part
     * @param checksum                  the checksum
     */
    @SuppressWarnings("InconsistentOverloads")
    TopicId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = checksum;
    }

    /**
     * Create a topic id from a string.
     *
     * @param id                        the string representation
     * @return                          the new topic id
     */
    public static TopicId fromString(String id) {
        return EntityIdHelper.fromString(id, TopicId::new);
    }

    /**
     * Create a topic id from a solidity address.
     *
     * @param address                   the solidity address
     * @return                          the new topic id
     */
    public static TopicId fromSolidityAddress(String address) {
        return EntityIdHelper.fromSolidityAddress(address, TopicId::new);
    }

    /**
     * Create a topic id from a protobuf.
     *
     * @param topicId                   the protobuf
     * @return                          the new topic id
     */
    static TopicId fromProtobuf(TopicID topicId) {
        Objects.requireNonNull(topicId);

        return new TopicId(topicId.getShardNum(), topicId.getRealmNum(), topicId.getTopicNum());
    }

    /**
     * Create a topic id from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new topic id
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static TopicId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TopicID.parseFrom(bytes).toBuilder().build());
    }

    /**
     * Extract the solidity address representation.
     *
     * @return                          the solidity address representation
     */
    public String toSolidityAddress() {
        return EntityIdHelper.toSolidityAddress(shard, realm, num);
    }

    /**
     * Extracts a protobuf representing the token id.
     *
     * @return                          the protobuf representation
     */
    TopicID toProtobuf() {
        return TopicID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setTopicNum(num)
            .build();
    }

    /**
     * @param client to validate against
     * @throws BadEntityIdException if entity ID is formatted poorly
     * @deprecated Use {@link #validateChecksum(Client)} instead.
     */
    @Deprecated
    public void validate(Client client) throws BadEntityIdException {
        validateChecksum(client);
    }

    /**
     * Verify that the client has a valid checksum.
     *
     * @param client                    the client to verify
     * @throws BadEntityIdException     if entity ID is formatted poorly
     */
    public void validateChecksum(Client client) throws BadEntityIdException {
        EntityIdHelper.validate(shard, realm, num, client, checksum);
    }

    /**
     * Extracts the checksum.
     *
     * @return                          the checksum
     */
    @Nullable
    public String getChecksum() {
        return checksum;
    }

    /**
     * Extracts a byte array representation.
     *
     * @return                          the byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return EntityIdHelper.toString(shard, realm, num);
    }

    /**
     * Create a string representation that includes the checksum.
     *
     * @param client                    the client
     * @return                          the string representation with the checksum
     */
    public String tostringwithchecksum(Client client) {
        return EntityIdHelper.toStringWithChecksum(shard, realm, num, client, checksum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, num);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TopicId)) {
            return false;
        }

        TopicId otherId = (TopicId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
    }

    @Override
    public int compareTo(TopicId o) {
        Objects.requireNonNull(o);
        int shardComparison = Long.compare(shard, o.shard);
        if (shardComparison != 0) {
            return shardComparison;
        }
        int realmComparison = Long.compare(realm, o.realm);
        if (realmComparison != 0) {
            return realmComparison;
        }
        return Long.compare(num, o.num);
    }
}
