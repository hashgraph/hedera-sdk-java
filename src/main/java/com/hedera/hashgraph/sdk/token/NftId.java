package com.hedera.hashgraph.sdk.token;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnegative;
import java.util.Objects;

@Beta
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
        String[] parts = id.split("@");
        if(parts.length != 2) {
            throw new IllegalArgumentException("Expecting {shardNum}.{realmNum}.{idNum}@{serialNum}");
        }
        try {
            return new NftId(TokenId.fromString(parts[0]), Long.parseLong(parts[1]));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Id format, should be in format {shardNum}.{realmNum}.{idNum}@{serialNum}", e);
        }
    }

    NftId(com.hedera.hashgraph.proto.NftID nftId) {
        tokenId = new TokenId(nftId.getTokenID());
        serial = nftId.getSerialNumber();
    }

    @Override
    public String toString() {
        // temporary
        return tokenId.toString() + "@" + serial;
    }

    com.hedera.hashgraph.proto.NftID toProto() {
        return com.hedera.hashgraph.proto.NftID.newBuilder()
            .setTokenID(tokenId.toProto())
            .setSerialNumber(serial)
            .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenId.shard, tokenId.realm, tokenId.token, serial);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NftId)) return false;

        NftId otherId = (NftId) o;
        return tokenId.equals(otherId.tokenId) && serial == otherId.serial;
    }
}
