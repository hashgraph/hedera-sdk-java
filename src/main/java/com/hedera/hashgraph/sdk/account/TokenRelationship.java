package com.hedera.hashgraph.sdk.account;

import com.google.common.base.MoreObjects;
import com.hedera.hashgraph.proto.TokenFreezeStatus;
import com.hedera.hashgraph.proto.TokenKycStatus;
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
        this.kycStatus = kycStatus == TokenKycStatus.KycNotApplicable_VALUE ? null : kycStatus == TokenKycStatus.Granted_VALUE;
        this.freezeStatus = freezeStatus == TokenFreezeStatus.FreezeNotApplicable_VALUE ? null : freezeStatus == TokenFreezeStatus.Frozen_VALUE;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tokenId", tokenId)
            .add("symbol", symbol)
            .add("balance", balance)
            .add("kycStatus", kycStatus)
            .add("freezeStatus", freezeStatus)
            .toString();
    }
}
