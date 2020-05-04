package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TopicID;

import javax.annotation.Nonnegative;

/**
 * Unique identifier for a topic (used by the consensus service).
 */
public final class TopicId extends EntityId {
    public TopicId(@Nonnegative long num) {
        super(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public TopicId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        super(shard, realm, num);
    }

    public static TopicId fromString(String id) {
        return EntityId.fromString(id, TopicId::new);
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
}
