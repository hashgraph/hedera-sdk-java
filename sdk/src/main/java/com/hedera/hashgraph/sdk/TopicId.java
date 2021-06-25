package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TopicID;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
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

    @Nullable
    private String checksum;

    public TopicId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public TopicId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = null;
    }

    TopicId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable NetworkName network, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;

        if (network != null) {
            if (checksum == null) {
                this.checksum = EntityIdHelper.checksum(Integer.toString(network.id), shard + "." + realm + "." + num);
            } else {
                this.checksum = checksum;
            }
        } else {
            this.checksum = null;
        }
    }

    public static TopicId fromString(String id) {
        return EntityIdHelper.fromString(id, TopicId::new);
    }

    static TopicId fromProtobuf(TopicID topicId, @Nullable NetworkName networkName) {
        Objects.requireNonNull(topicId);

        var id = new TopicId(topicId.getShardNum(), topicId.getRealmNum(), topicId.getTopicNum());

        if (networkName != null) {
            id.setNetwork(networkName);
        }

        return id;
    }

    static TopicId fromProtobuf(TopicID topicId) {
        return TopicId.fromProtobuf(topicId, null);
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

    TopicId setNetworkWith(Client client) {
        if (client.network.networkName != null) {
            setNetwork(client.network.networkName);
        }

        return this;
    }

    TopicId setNetwork(NetworkName name) {
        checksum = EntityIdHelper.checksum(Integer.toString(name.id), EntityIdHelper.toString(shard, realm, num));
        return this;
    }

    public void validate(Client client) {
        EntityIdHelper.validate(shard, realm, num, client, checksum);
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return EntityIdHelper.toString(shard, realm, num, checksum);
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
