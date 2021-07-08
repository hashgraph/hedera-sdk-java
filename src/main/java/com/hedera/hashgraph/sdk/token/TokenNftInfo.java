package com.hedera.hashgraph.sdk.token;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.account.AccountId;

import java.time.Instant;
import java.util.Objects;

@Beta
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

    TokenNftInfo(com.hedera.hashgraph.proto.TokenNftInfo info) {
        this.nftId = new NftId(info.getNftID());
        this.accountId = new AccountId(info.getAccountID());
        this.creationTime = TimestampHelper.timestampTo(info.getCreationTime());
        this.metadata = info.getMetadata().toByteArray();
    }

    public static TokenNftInfo fromResponse(com.hedera.hashgraph.proto.TokenGetNftInfoResponse response) {
        return new TokenNftInfo(response.getNft());
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
}
