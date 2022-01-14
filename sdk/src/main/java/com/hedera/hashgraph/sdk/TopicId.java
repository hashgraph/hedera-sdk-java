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

    public TopicId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public TopicId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this(shard, realm, num, null);
    }

    @SuppressWarnings("InconsistentOverloads")
    TopicId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = checksum;
    }

    //Constructs a topic ID from string
    public static TopicId fromString(String id) {
        return EntityIdHelper.fromString(id, TopicId::new);
    }
    
    //Constructs a topic ID from a solidity address
    public static TopicId fromSolidityAddress(String address) {
        return EntityIdHelper.fromSolidityAddress(address, TopicId::new);
    }

    static TopicId fromProtobuf(TopicID topicId) {
        Objects.requireNonNull(topicId);

        return new TopicId(topicId.getShardNum(), topicId.getRealmNum(), topicId.getTopicNum());
    }

    //Constructs a topic ID from bytes
    public static TopicId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TopicID.parseFrom(bytes).toBuilder().build());
    }
    
    //Constructs a solidity address from topic ID
    public String toSolidityAddress() {
        return EntityIdHelper.toSolidityAddress(shard, realm, num);
    }


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

    public void validateChecksum(Client client) throws BadEntityIdException {
        EntityIdHelper.validate(shard, realm, num, client, checksum);
    }

    @Nullable
    public String getChecksum() {
        return checksum;
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return EntityIdHelper.toString(shard, realm, num);
    }

    public String toStringWithChecksum(Client client) {
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
