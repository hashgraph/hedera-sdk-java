package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TopicID;

import javax.annotation.Nonnegative;
import java.util.Objects;

/**
 * Unique identifier for a topic (used by the consensus service).
 */
public final class TopicId {
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

    public TopicId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public TopicId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
    }

    public static TopicId fromString(String id) {
        return EntityIdHelper.fromString(id, TopicId::new);
    }

    static TopicId fromProtobuf(TopicID topicId) {
        return new TopicId(topicId.getShardNum(), topicId.getRealmNum(), topicId.getTopicNum());
    }

    public static TopicId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TopicID.parseFrom(bytes).toBuilder().build());
    }

    TopicID toProtobuf() {
        return TopicID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setTopicNum(num)
            .build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return "" + shard + "." + realm + "." + num;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, num);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TopicId)) return false;

        TopicId otherId = (TopicId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
    }
}
