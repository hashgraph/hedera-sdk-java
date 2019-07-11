package com.hedera.hashgraph.sdk.consensus;

import com.google.common.base.Splitter;
import com.hedera.hashgraph.sdk.Entity;
import com.hedera.hashgraph.sdk.proto.TopicID;
import com.hedera.hashgraph.sdk.proto.TopicIDOrBuilder;

import java.util.Objects;

public final class TopicId implements Entity {
    private final TopicID.Builder inner;

    public TopicId(long topicNum) {
        this(0, 0, topicNum);
    }

    public TopicId(long shardNum, long realmNum, long topicNum) {
        inner = TopicID.newBuilder()
            .setRealmNum(realmNum)
            .setShardNum(shardNum)
            .setTopicNum(topicNum);
    }

    public static TopicId fromString(String topicId) throws IllegalArgumentException {
        var rawNums = Splitter.on('.')
            .split(topicId)
            .iterator();

        var newTopicId = TopicID.newBuilder();

        try {
            newTopicId.setRealmNum(Integer.parseInt(rawNums.next()));
            newTopicId.setShardNum(Integer.parseInt(rawNums.next()));
            newTopicId.setTopicNum(Integer.parseInt(rawNums.next()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Id format, should be in format {shardNum}.{realmNum}.{topicNum}");
        }

        return new TopicId(newTopicId);
    }

    public TopicId(TopicIDOrBuilder topicId) {
        this(topicId.getShardNum(), topicId.getRealmNum(), topicId.getTopicNum());
    }

    public long getShardNum() {
        return inner.getShardNum();
    }

    public long getRealmNum() {
        return inner.getRealmNum();
    }

    public long getTopicNum() {
        return inner.getTopicNum();
    }

    @Override
    public String toString() {
        return "" + getShardNum() + "." + getRealmNum() + "." + getTopicNum();
    }

    public TopicID toProto() {
        return inner.build();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (other == null || getClass() != other.getClass()) return false;

        var otherId = (TopicId) other;
        return otherId.getTopicNum() == getTopicNum() && otherId.getRealmNum() == getRealmNum()
                && otherId.getShardNum() == getShardNum();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTopicNum(), getRealmNum(), getShardNum());
    }
}
