package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenFreezeStatus;
import com.hedera.hashgraph.sdk.proto.TokenGetInfoResponse;
import com.hedera.hashgraph.sdk.proto.TokenKycStatus;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

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
    @Nullable
    public final Key adminKey;

    /**
     *
     */
    @Nullable
    public final Key kycKey;

    /**
     *
     */
    @Nullable
    public final Key freezeKey;

    /**
     *
     */
    @Nullable
    public final Key wipeKey;

    /**
     *
     */
    @Nullable
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
    @Nullable
    public final Duration autoRenewPeriod;

    /**
     *
     */
    @Nullable
    public final Instant expirationTime;

    public final String tokenMemo;

    private TokenInfo(
        TokenId tokenId,
        String name,
        String symbol,
        int decimals,
        long totalSupply,
        AccountId treasuryAccountId,
        @Nullable Key adminKey,
        @Nullable Key kycKey,
        @Nullable Key freezeKey,
        @Nullable Key wipeKey,
        @Nullable Key supplyKey,
        @Nullable Boolean defaultFreezeStatus,
        @Nullable Boolean defaultKycStatus,
        boolean isDeleted,
        @Nullable AccountId autoRenewAccount,
        @Nullable Duration autoRenewPeriod,
        @Nullable Instant expirationTime,
        String tokenMemo
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
        this.tokenMemo = tokenMemo;
    }

    @Nullable
    static Boolean freezeStatusFromProtobuf(TokenFreezeStatus freezeStatus) {
        return freezeStatus == TokenFreezeStatus.FreezeNotApplicable ? null : freezeStatus == TokenFreezeStatus.Frozen;
    }

    @Nullable
    static Boolean kycStatusFromProtobuf(TokenKycStatus kycStatus) {
        return kycStatus == TokenKycStatus.KycNotApplicable ? null : kycStatus == TokenKycStatus.Granted;
    }

    static TokenInfo fromProtobuf(TokenGetInfoResponse response) {
        var info = response.getTokenInfo();

        return new TokenInfo(
            TokenId.fromProtobuf(info.getTokenId()),
            info.getName(),
            info.getSymbol(),
            info.getDecimals(),
            info.getTotalSupply(),
            AccountId.fromProtobuf(info.getTreasury()),
            info.hasAdminKey() ? Key.fromProtobufKey(info.getAdminKey()) : null,
            info.hasKycKey() ? Key.fromProtobufKey(info.getKycKey()) : null,
            info.hasFreezeKey() ? Key.fromProtobufKey(info.getFreezeKey()) : null,
            info.hasWipeKey() ? Key.fromProtobufKey(info.getWipeKey()) : null,
            info.hasSupplyKey() ? Key.fromProtobufKey(info.getSupplyKey()) : null,
            freezeStatusFromProtobuf(info.getDefaultFreezeStatus()),
            kycStatusFromProtobuf(info.getDefaultKycStatus()),
            info.getDeleted(),
            info.hasAutoRenewAccount() ? AccountId.fromProtobuf(info.getAutoRenewAccount()) : null,
            info.hasAutoRenewPeriod() ? DurationConverter.fromProtobuf(info.getAutoRenewPeriod()) : null,
            info.hasExpiry() ? InstantConverter.fromProtobuf(info.getExpiry()) : null,
            info.getMemo()
        );
    }

    public static TokenInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TokenGetInfoResponse.parseFrom(bytes).toBuilder().build());
    }

    @Nullable
    static TokenFreezeStatus freezeStatusToProtobuf(@Nullable Boolean freezeStatus) {
        return freezeStatus == null ? TokenFreezeStatus.FreezeNotApplicable : freezeStatus ? TokenFreezeStatus.Frozen : TokenFreezeStatus.Unfrozen;
    }

    @Nullable
    static TokenKycStatus kycStatusToProtobuf(@Nullable Boolean kycStatus) {
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
                .setAdminKey(adminKey != null ? adminKey.toProtobufKey() : null)
                .setKycKey(kycKey != null ? kycKey.toProtobufKey() : null)
                .setFreezeKey(freezeKey != null ? freezeKey.toProtobufKey() : null)
                .setWipeKey(wipeKey != null ? wipeKey.toProtobufKey() : null)
                .setSupplyKey(supplyKey != null ? supplyKey.toProtobufKey() : null)
                .setDefaultFreezeStatus(freezeStatusToProtobuf(defaultFreezeStatus))
                .setDefaultKycStatus(kycStatusToProtobuf(defaultKycStatus))
                .setDeleted(isDeleted)
                .setAutoRenewAccount(autoRenewAccount != null ? autoRenewAccount.toProtobuf() : null)
                .setAutoRenewPeriod(autoRenewPeriod != null ? DurationConverter.toProtobuf(autoRenewPeriod) : null)
                .setExpiry(expirationTime != null ? InstantConverter.toProtobuf(expirationTime) : null)
                .setMemo(tokenMemo)
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
            .add("tokenMemo", tokenMemo)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
