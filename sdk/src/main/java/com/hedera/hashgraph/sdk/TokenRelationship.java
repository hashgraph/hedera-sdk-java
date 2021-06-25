package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenFreezeStatus;
import com.hedera.hashgraph.sdk.proto.TokenKycStatus;

import javax.annotation.Nullable;

public class TokenRelationship {
    public final TokenId tokenId;

    public final String symbol;

    public final long balance;

    @Nullable
    public final Boolean kycStatus;

    @Nullable
    public final Boolean freezeStatus;

    private TokenRelationship(TokenId tokenId, String symbol, long balance, @Nullable Boolean kycStatus, @Nullable Boolean freezeStatus) {
        this.tokenId = tokenId;
        this.symbol = symbol;
        this.balance = balance;
        this.kycStatus = kycStatus;
        this.freezeStatus = freezeStatus;
    }

    @Nullable static Boolean freezeStatusFromProtobuf(TokenFreezeStatus freezeStatus) {
        return freezeStatus == TokenFreezeStatus.FreezeNotApplicable ? null : freezeStatus == TokenFreezeStatus.Frozen;
    }

    @Nullable static Boolean kycStatusFromProtobuf(TokenKycStatus kycStatus) {
        return kycStatus == TokenKycStatus.KycNotApplicable ? null : kycStatus == TokenKycStatus.Granted;
    }

    static TokenRelationship fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenRelationship tokenRelationship) {
        return TokenRelationship.fromProtobuf(tokenRelationship, null);
    }

    static TokenRelationship fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenRelationship tokenRelationship, @Nullable NetworkName networkName) {
        return new TokenRelationship(
            TokenId.fromProtobuf(tokenRelationship.getTokenId(), networkName),
            tokenRelationship.getSymbol(),
            tokenRelationship.getBalance(),
            kycStatusFromProtobuf(tokenRelationship.getKycStatus()),
            freezeStatusFromProtobuf(tokenRelationship.getFreezeStatus())
        );
    }

    public static TokenRelationship fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenRelationship.parseFrom(bytes).toBuilder().build());
    }

    @Nullable static TokenFreezeStatus freezeStatusToProtobuf(@Nullable Boolean freezeStatus) {
        return freezeStatus == null ? TokenFreezeStatus.FreezeNotApplicable : freezeStatus ? TokenFreezeStatus.Frozen : TokenFreezeStatus.Unfrozen;
    }

    @Nullable static TokenKycStatus kycStatusToProtobuf(@Nullable Boolean kycStatus) {
        return kycStatus == null ? TokenKycStatus.KycNotApplicable : kycStatus ? TokenKycStatus.Granted : TokenKycStatus.Revoked;
    }

    com.hedera.hashgraph.sdk.proto.TokenRelationship toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.TokenRelationship.newBuilder()
            .setTokenId(tokenId.toProtobuf())
            .setSymbol(symbol)
            .setBalance(balance)
            .setKycStatus(kycStatusToProtobuf(kycStatus))
            .setFreezeStatus(freezeStatusToProtobuf(freezeStatus))
            .build();
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

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
