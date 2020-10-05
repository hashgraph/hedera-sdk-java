package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenID;

import javax.annotation.Nonnegative;

public class TokenId extends EntityId {
    public TokenId(@Nonnegative long num) {
        super(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public TokenId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        super(shard, realm, num);
    }

    public static TokenId fromString(String id) {
        return EntityId.fromString(id, TokenId::new);
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
}
