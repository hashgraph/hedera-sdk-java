package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TopicID;

import javax.annotation.Nonnegative;

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

    static TopicId fromProtobuf(TopicID fileId) {
        return new TopicId(fileId.getShardNum(), fileId.getRealmNum(), fileId.getTopicNum());
    }

    TopicID toProtobuf() {
        return TopicID.newBuilder().setShardNum(shard).setRealmNum(realm).setTopicNum(num).build();
    }

    byte[] toBytes() {
        return this.toProtobuf().toByteArray();
    }

    TopicId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TopicID.parseFrom(bytes).toBuilder().build());
    }
}
