package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.NftID;

import javax.annotation.Nonnegative;
import java.util.Objects;
import javax.annotation.Nullable;

public class NftId {
    /**
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

    public static NftId fromString(String id) {
        @SuppressWarnings("StringSplitter")
        var parts = id.split("@");
        if(parts.length != 2) {
            throw new IllegalArgumentException("Expecting {shardNum}.{realmNum}.{idNum}-{checksum}@{serialNum}");
        }
        return new NftId(TokenId.fromString(parts[0]), Long.parseLong(parts[1]));
    }

    static NftId fromProtobuf(NftID nftId, @Nullable NetworkName networkName) {
        Objects.requireNonNull(nftId);
        var tokenId = nftId.getTokenID();
        var returnNftId = new NftId(TokenId.fromProtobuf(tokenId), nftId.getSerialNumber());
        if(networkName != null) {
            returnNftId.tokenId.setNetwork(networkName);
        }
        return returnNftId;
    }

    static NftId fromProtobuf(NftID nftId) {
        return fromProtobuf(nftId, null);
    }

    public static NftId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(NftID.parseFrom(bytes).toBuilder().build());
    }

    NftID toProtobuf() {
        return NftID.newBuilder()
            .setTokenID(tokenId.toProtobuf())
            .setSerialNumber(serial)
            .build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
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
