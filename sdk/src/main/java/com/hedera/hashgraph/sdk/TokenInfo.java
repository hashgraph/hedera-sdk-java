package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenFreezeStatus;
import com.hedera.hashgraph.sdk.proto.TokenGetInfoResponse;
import com.hedera.hashgraph.sdk.proto.TokenKycStatus;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;

public class TokenInfo {
    /**
     * The ID of the token for which information is requested.
     */
    public final TokenId tokenId;

    /**
     * Name of token.
     */
    public final String name;

    /**
     * Symbol of token.
     */
    public final String symbol;

    /**
     * The amount of decimal places that this token supports.
     */
    public final int decimals;

    /**
     * Total Supply of token.
     */
    public final long totalSupply;

    /**
     *
     */
    public final AccountId treasuryAccountId;

    /**
     *
     */
    public final Key adminKey;

    /**
     *
     */
    public final Key kycKey;

    /**
     *
     */
    public final Key freezeKey;

    /**
     *
     */
    public final Key wipeKey;

    /**
     *
     */
    public final Key supplyKey;

    /**
     *
     */
    @Nullable
    public final Boolean defaultFreezeStatus;

    /**
     *
     */
    @Nullable
    public final Boolean defaultKycStatus;

    /**
     *
     */
    public final boolean isDeleted;

    /**
     *
     */
    @Nullable
    public final AccountId autoRenewAccount;

    /**
     *
     */
    public final Duration autoRenewPeriod;

    /**
     *
     */
    public final Instant expirationTime;

    private TokenInfo(
        TokenId tokenId,
        String name,
        String symbol,
        int decimals,
        long totalSupply,
        AccountId treasuryAccountId,
        Key adminKey,
        Key kycKey,
        Key freezeKey,
        Key wipeKey,
        Key supplyKey,
        @Nullable Boolean defaultFreezeStatus,
        @Nullable Boolean defaultKycStatus,
        boolean isDeleted,
        AccountId autoRenewAccount,
        Duration autoRenewPeriod,
        Instant expirationTime
    ) {
        this.tokenId = tokenId;
        this.name = name;
        this.symbol = symbol;
        this.decimals = decimals;
        this.totalSupply = totalSupply;
        this.treasuryAccountId = treasuryAccountId;
        this.adminKey = adminKey;
        this.kycKey = kycKey;
        this.freezeKey = freezeKey;
        this.wipeKey = wipeKey;
        this.supplyKey = supplyKey;
        this.defaultFreezeStatus = defaultFreezeStatus;
        this.defaultKycStatus = defaultKycStatus;
        this.isDeleted = isDeleted;
        this.autoRenewAccount = autoRenewAccount;
        this.autoRenewPeriod = autoRenewPeriod;
        this.expirationTime = expirationTime;
    }

    @Nullable static Boolean freezeStatusFromProtobuf(TokenFreezeStatus freezeStatus) {
        return freezeStatus == TokenFreezeStatus.FreezeNotApplicable ? null : freezeStatus == TokenFreezeStatus.Frozen;
    }

    @Nullable static Boolean kycStatusFromProtobuf(TokenKycStatus kycStatus) {
        return kycStatus == TokenKycStatus.KycNotApplicable ? null : kycStatus == TokenKycStatus.Granted;
    }

    static TokenInfo fromProtobuf(TokenGetInfoResponse tokenInfo) {
        return new TokenInfo(
            TokenId.fromProtobuf(tokenInfo.getTokenInfo().getTokenId()),
            tokenInfo.getTokenInfo().getName(),
            tokenInfo.getTokenInfo().getSymbol(),
            tokenInfo.getTokenInfo().getDecimals(),
            tokenInfo.getTokenInfo().getTotalSupply(),
            AccountId.fromProtobuf(tokenInfo.getTokenInfo().getTreasury()),
            Key.fromProtobuf(tokenInfo.getTokenInfo().getAdminKey()),
            Key.fromProtobuf(tokenInfo.getTokenInfo().getKycKey()),
            Key.fromProtobuf(tokenInfo.getTokenInfo().getFreezeKey()),
            Key.fromProtobuf(tokenInfo.getTokenInfo().getWipeKey()),
            Key.fromProtobuf(tokenInfo.getTokenInfo().getSupplyKey()),
            freezeStatusFromProtobuf(tokenInfo.getTokenInfo().getDefaultFreezeStatus()),
            kycStatusFromProtobuf(tokenInfo.getTokenInfo().getDefaultKycStatus()),
            tokenInfo.getTokenInfo().getDeleted(),
            AccountId.fromProtobuf(tokenInfo.getTokenInfo().getAutoRenewAccount()),
            DurationConverter.fromProtobuf(tokenInfo.getTokenInfo().getAutoRenewPeriod()),
            InstantConverter.fromProtobuf(tokenInfo.getTokenInfo().getExpiry())
        );
    }

    public static TokenInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TokenGetInfoResponse.parseFrom(bytes).toBuilder().build());
    }

    @Nullable static TokenFreezeStatus freezeStatusToProtobuf(@Nullable Boolean freezeStatus) {
        return freezeStatus == null ? TokenFreezeStatus.FreezeNotApplicable : freezeStatus ? TokenFreezeStatus.Frozen : TokenFreezeStatus.Unfrozen;
    }

    @Nullable static TokenKycStatus kycStatusToProtobuf(@Nullable Boolean kycStatus) {
        return kycStatus == null ? TokenKycStatus.KycNotApplicable : kycStatus ? TokenKycStatus.Granted : TokenKycStatus.Revoked;
    }

    TokenGetInfoResponse toProtobuf() {
        return TokenGetInfoResponse.newBuilder().setTokenInfo(
            com.hedera.hashgraph.sdk.proto.TokenInfo.newBuilder()
            .setTokenId(tokenId.toProtobuf())
            .setName(name)
            .setSymbol(symbol)
            .setDecimals(decimals)
            .setTotalSupply(totalSupply)
            .setTreasury(treasuryAccountId.toProtobuf())
            .setAdminKey(adminKey.toKeyProtobuf())
            .setKycKey(kycKey.toKeyProtobuf())
            .setFreezeKey(freezeKey.toKeyProtobuf())
            .setWipeKey(wipeKey.toKeyProtobuf())
            .setSupplyKey(supplyKey.toKeyProtobuf())
            .setDefaultFreezeStatus(freezeStatusToProtobuf(defaultFreezeStatus))
            .setDefaultKycStatus(kycStatusToProtobuf(defaultKycStatus))
            .setDeleted(isDeleted)
            .setAutoRenewAccount(autoRenewAccount != null ? autoRenewAccount.toProtobuf() : null)
            .setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod))
            .setExpiry(InstantConverter.toProtobuf(expirationTime))
        ).build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tokenId", tokenId)
            .add("name", name)
            .add("symbol", symbol)
            .add("decimals", decimals)
            .add("totalSupply", totalSupply)
            .add("treasuryAccountId", treasuryAccountId)
            .add("adminKey", adminKey)
            .add("kycKey", kycKey)
            .add("freezeKey", freezeKey)
            .add("wipeKey", wipeKey)
            .add("supplyKey", supplyKey)
            .add("defaultFreezeStatus", defaultFreezeStatus)
            .add("defaultKycStatus", defaultKycStatus)
            .add("isDeleted", isDeleted)
            .add("autoRenewAccount", autoRenewAccount)
            .add("autoRenewPeriod", autoRenewPeriod)
            .add("expirationTime", expirationTime)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
