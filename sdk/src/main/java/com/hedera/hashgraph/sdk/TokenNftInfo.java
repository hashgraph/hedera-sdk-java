package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenGetNftInfoResponse;
import com.hedera.hashgraph.sdk.proto.TokenGetNftInfo;
import org.threeten.bp.Instant;
import java.util.Objects;
import javax.annotation.Nullable;

public class TokenNftInfo {
    /**
     * The ID of the NFT
     */
    public final NftId nftId;

    /**
     * The current owner of the NFT
     */
    public final AccountId accountId;

    /**
     * The effective consensus timestamp at which the NFT was minted
     */
    public final Instant creationTime;

    /**
     * Represents the unique metadata of the NFT
     */
    public final byte[] metadata;

    private TokenNftInfo(
        NftId nftId,
        AccountId accountId,
        Instant creationTime,
        byte[] metadata
    ) {
        this.nftId = nftId;
        this.accountId = accountId;
        this.creationTime = Objects.requireNonNull(creationTime);
        this.metadata = metadata;
    }

    static TokenNftInfo fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenNftInfo info, @Nullable NetworkName networkName) {
        return new TokenNftInfo(
            NftId.fromProtobuf(info.getNftID(), networkName),
            AccountId.fromProtobuf(info.getAccountID(), networkName),
            InstantConverter.fromProtobuf(info.getCreationTime()),
            info.getMetadata().toByteArray()
        );
    }

    static TokenNftInfo fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenNftInfo info) {
        return fromProtobuf(info, null);
    }

    public static TokenNftInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TokenGetNftInfoResponse.parseFrom(bytes).toBuilder().build().getNft());
    }

    TokenGetNftInfoResponse toProtobuf() {
        return TokenGetNftInfoResponse.newBuilder().setNft(
            com.hedera.hashgraph.sdk.proto.TokenNftInfo.newBuilder()
                .setNftID(nftId.toProtobuf())
                .setAccountID(accountId.toProtobuf())
                .setCreationTime(InstantConverter.toProtobuf(creationTime))
                .setMetadata(ByteString.copyFrom(metadata))
        ).build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("nftId", nftId)
            .add("accountId", accountId)
            .add("creationTime", creationTime)
            .add("metadata", metadata)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
