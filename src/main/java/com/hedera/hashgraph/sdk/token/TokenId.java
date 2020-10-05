package com.hedera.hashgraph.sdk.token;

import com.hedera.hashgraph.proto.TokenID;
import com.hedera.hashgraph.proto.TokenIDOrBuilder;
import com.hedera.hashgraph.sdk.IdUtil;
import com.hedera.hashgraph.sdk.Internal;
import com.hedera.hashgraph.sdk.SolidityUtil;

import java.util.Objects;

public final class TokenId {
    public final long shard;
    public final long realm;
    public final long token;

    /** Constructs an `TokenId` with `0` for `shard` and `realm` (e.g., `0.0.<tokenNum>`). */
    public TokenId(long tokenNum) {
        this(0, 0, tokenNum);
    }

    public TokenId(long shard, long realm, long token) {
        this.shard = shard;
        this.realm = realm;
        this.token = token;
    }

    /** Constructs an `TokenId` from a string formatted as <shardNum>.<realmNum>.<tokenNum> */
    public static TokenId fromString(String token) throws IllegalArgumentException {
        return IdUtil.parseIdString(token, TokenId::new);
    }

    public TokenId(TokenIDOrBuilder tokenId) {
        this(tokenId.getShardNum(), tokenId.getRealmNum(), tokenId.getTokenNum());
    }

    public static TokenId fromSolidityAddress(String address) {
        return SolidityUtil.parseAddress(address, TokenId::new);
    }

    @Override
    public String toString() {
        return "" + shard + "." + realm + "." + token;
    }

    public String toSolidityAddress() {
        return SolidityUtil.addressFor(this);
    }

    @Internal
    public TokenID toProto() {
        return TokenID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setTokenNum(token)
            .build();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (other == null || getClass() != other.getClass()) return false;

        TokenId otherId = (TokenId) other;
        return otherId.token == token
            && otherId.realm == realm
            && otherId.shard == shard;
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, realm, shard);
    }
}
