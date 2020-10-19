package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.token.TokenId;

import javax.annotation.Nullable;

public class TokenRelationship {
    public final TokenId tokenId;

    public final String symbol;

    public final long balance;

    @Nullable
    public final Boolean kycStatus;

    @Nullable
    public final Boolean freezeStatus;

    TokenRelationship(com.hedera.hashgraph.proto.TokenRelationship relationship) {
        int freezeStatus = relationship.getFreezeStatus() == null ? 0 : relationship.getFreezeStatus().getNumber();
        int kycStatus = relationship.getKycStatus() == null ? 0 : relationship.getKycStatus().getNumber();

        this.tokenId = new TokenId(relationship.getTokenId());
        this.symbol = relationship.getSymbol();
        this.balance = relationship.getBalance();
        this.kycStatus = kycStatus == 0 ? null : kycStatus == 2;
        this.freezeStatus = freezeStatus == 0 ? null : freezeStatus == 2;
    }
}
