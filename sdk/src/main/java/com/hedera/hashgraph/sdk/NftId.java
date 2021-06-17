package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;

import javax.annotation.Nonnegative;
import java.util.Objects;

public class NftId {
    /*
     * The (non-fungible) token of which this NFT is an instance
     */
    public final TokenId tokenId;

    /**
     * The unique identifier of this instance
     */
    @Nonnegative
    public final long serial;

    public NftId(TokenId tokenId, @Nonnegative long serial) {
        this.tokenId = Objects.requireNonNull(tokenId);
        this.serial = serial;
    }

    public NftId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num, @Nonnegative long serial) {
        this(new TokenId(shard, realm, num), serial);
    }

    public static NftId fromString(String id) {
        // temporary
        var parts = id.split("@");
        if(parts.length != 2) {
            throw new IllegalArgumentException("Expecting {shardNum}.{realmNum}.{idNum}@{serialNum}");
        }
        try {
            return new NftId(TokenId.fromString(parts[0]), Long.parseLong(parts[1]));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Id format, should be in format {shardNum}.{realmNum}.{idNum}@{serialNum}", e);
        }
    }

    static NftId fromProtobuf(com.hedera.hashgraph.sdk.proto.NftID nftId) {
        var tokenId = nftId.getTokenID();
        return new NftId(TokenId.fromProtobuf(tokenId), nftId.getSerialNumber());
    }

    public static NftId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.NftID.parseFrom(bytes).toBuilder().build());
    }

    com.hedera.hashgraph.sdk.proto.NftID toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.NftID.newBuilder()
            .setTokenID(tokenId.toProtobuf())
            .setSerialNumber(serial)
            .build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        // temporary
        return tokenId.toString() + "@" + serial;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenId.shard, tokenId.realm, tokenId.num, serial);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NftId)) return false;

        NftId otherId = (NftId) o;
        return tokenId.equals(otherId.tokenId) && serial == otherId.serial;
    }
}