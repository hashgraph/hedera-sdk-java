package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.proto.TopicID;
import com.hedera.hashgraph.proto.TopicIDOrBuilder;
import com.hedera.hashgraph.sdk.IdUtil;
import com.hedera.hashgraph.sdk.Internal;

import java.util.Objects;

public final class ConsensusTopicId {
    public final long shard;
    public final long realm;
    public final long topic;

    public ConsensusTopicId(long shard, long realm, long topic) {
        this.shard = shard;
        this.realm = realm;
        this.topic = topic;
    }

    public ConsensusTopicId(long topic) {
        this(0, 0, topic);
    }

    @Internal
    public ConsensusTopicId(TopicIDOrBuilder topicID) {
        this(topicID.getShardNum(), topicID.getRealmNum(), topicID.getTopicNum());
    }

    /** Constructs a `TopicId` from a string formatted as <shardNum>.<realmNum>.<topicNum> */
    public static ConsensusTopicId fromString(String topic) throws IllegalArgumentException {
        return IdUtil.parseIdString(topic, ConsensusTopicId::new);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, topic);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (!(other instanceof ConsensusTopicId)) return false;

        ConsensusTopicId otherId = (ConsensusTopicId) other;
        return shard == otherId.shard
            && realm == otherId.realm
            && topic == otherId.topic;
    }

    @Internal
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
