package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenID;

import javax.annotation.Nonnegative;
import java.util.Objects;

public class TokenId {
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

    public TokenId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public TokenId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
    }

    public static TokenId fromString(String id) {
        return EntityIdHelper.fromString(id, TokenId::new);
    }

    static TokenId fromProtobuf(TokenID topicId) {
        return new TokenId(topicId.getShardNum(), topicId.getRealmNum(), topicId.getTokenNum());
    }

    public static TokenId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TokenID.parseFrom(bytes).toBuilder().build());
    }

    TokenID toProtobuf() {
        return TokenID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setTokenNum(num)
            .build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        var checksum = EntityIdHelper.parseAddress("","" + shard + "." + realm + "." + num);
        return "" + shard + "." + realm + "." + num + "-" + checksum.correctChecksum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, num);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TokenId)) return false;

        TokenId otherId = (TokenId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
    }
}
