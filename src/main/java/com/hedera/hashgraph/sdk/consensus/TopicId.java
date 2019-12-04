package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.Entity;
import com.hedera.hashgraph.sdk.IdUtil;
import com.hederahashgraph.api.proto.java.TopicID;
import com.hederahashgraph.api.proto.java.TopicIDOrBuilder;

import java.util.Objects;

public final class TopicId implements Entity {
    public final long shard;
    public final long realm;
    public final long topic;

    public TopicId(long shard, long realm, long topic) {
        this.shard = shard;
        this.realm = realm;
        this.topic = topic;
    }

    public TopicId(long topic) {
        this(0, 0, topic);
    }

    public TopicId(TopicIDOrBuilder topicID) {
        this(topicID.getShardNum(), topicID.getRealmNum(), topicID.getTopicNum());
    }

    /** Constructs a `TopicId` from a string formatted as <shardNum>.<realmNum>.<topicNum> */
    public static TopicId fromString(String topic) throws IllegalArgumentException {
        return IdUtil.parseIdString(topic, TopicId::new);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, topic);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (!(other instanceof TopicId)) return false;

        TopicId otherId = (TopicId) other;
        return shard == otherId.shard
            && realm == otherId.realm
            && topic == otherId.topic;
    }

    public TopicID toProto() {
        return TopicID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setTopicNum(topic)
            .build();
    }

    @Override
    public String toString() {
        return "" + shard + "." + realm + "." + topic;
    }
}
