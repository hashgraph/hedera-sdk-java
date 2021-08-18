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
    private final String checksum;

    public TokenId(@Nonnegative long num) {
        this(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public TokenId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        this(shard, realm, num, null);
    }

    @SuppressWarnings("InconsistentOverloads")
    TokenId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nullable String checksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.checksum = checksum;
    }

    public static TokenId fromString(String id) {
        return EntityIdHelper.fromString(id, TokenId::new);
    }

    static TokenId fromProtobuf(TokenID tokenId) {
        Objects.requireNonNull(tokenId);
        return new TokenId(tokenId.getShardNum(), tokenId.getRealmNum(), tokenId.getTokenNum());
    }

    public static TokenId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TokenID.parseFrom(bytes).toBuilder().build());
    }

    public NftId nft(@Nonnegative long serial) {
        return new NftId(this, serial);
    }

    TokenID toProtobuf() {
        return TokenID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setTokenNum(num)
            .build();
    }

    /**
     * @param client
     * @throws BadEntityIdException
     * @deprecated Use {@link #validateChecksum(Client)} instead.
     */
    @Deprecated
    public void validate(Client client) throws BadEntityIdException {
        validateChecksum(client);
    }

    public void validateChecksum(Client client) throws BadEntityIdException {
        EntityIdHelper.validate(shard, realm, num, client, checksum);
    }

    @Nullable
    public String getChecksum() {
        return checksum;
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return EntityIdHelper.toString(shard, realm, num);
    }

    public String toStringWithChecksum(Client client) {
        return EntityIdHelper.toStringWithChecksum(shard, realm, num, client, checksum);
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
