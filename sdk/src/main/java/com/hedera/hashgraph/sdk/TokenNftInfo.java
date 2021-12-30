package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenGetNftInfoResponse;
import org.threeten.bp.Instant;

import java.util.Objects;

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

    /**
     * The ledger ID the response was returned from; please see <a href="https://github.com/hashgraph/hedera-improvement-proposal/blob/master/HIP/hip-198.md">HIP-198</a> for the network-specific IDs.
     */
    public final LedgerId ledgerId;

    private TokenNftInfo(
        NftId nftId,
        AccountId accountId,
        Instant creationTime,
        byte[] metadata,
        LedgerId ledgerId
    ) {
        this.nftId = nftId;
        this.accountId = accountId;
        this.creationTime = Objects.requireNonNull(creationTime);
        this.metadata = metadata;
        this.ledgerId = ledgerId;
    }

    static TokenNftInfo fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenNftInfo info) {
        return new TokenNftInfo(
            NftId.fromProtobuf(info.getNftID()),
            AccountId.fromProtobuf(info.getAccountID()),
            InstantConverter.fromProtobuf(info.getCreationTime()),
            info.getMetadata().toByteArray(),
            LedgerId.fromByteString(info.getLedgerId())
        );
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
                .setLedgerId(ledgerId.toByteString())
        ).build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("nftId", nftId)
            .add("accountId", accountId)
            .add("creationTime", creationTime)
            .add("metadata", metadata)
            .add("ledgerId", ledgerId)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
