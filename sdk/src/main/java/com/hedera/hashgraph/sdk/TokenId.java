package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenID;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
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

    @Nullable
    private String checksum;

    public TokenId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public TokenId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = null;
    }

    TokenId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable NetworkName network, @Nullable String checksum) {
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

    public static TokenId fromString(String id) {
        return EntityIdHelper.fromString(id, TokenId::new);
    }

    static TokenId fromProtobuf(TokenID tokenId, @Nullable NetworkName networkName) {
        Objects.requireNonNull(tokenId);

        var id = new TokenId(tokenId.getShardNum(), tokenId.getRealmNum(), tokenId.getTokenNum());

        if (networkName != null) {
            id.setNetwork(networkName);
        }

        return id;
    }

    static TokenId fromProtobuf(TokenID tokenId) {
        return TokenId.fromProtobuf(tokenId, null);
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

    TokenId setNetworkWith(Client client) {
        if (client.network.networkName != null) {
            setNetwork(client.network.networkName);
        }

        return this;
    }

    TokenId setNetwork(NetworkName name) {
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
        if (!(o instanceof TokenId)) return false;

        TokenId otherId = (TokenId) o;
        return shard == otherId.shard && realm == otherId.realm && num == otherId.num;
    }
}
