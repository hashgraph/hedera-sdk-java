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
     * The ID of the account which is set as Treasury
     */
    public final AccountId treasuryAccountId;

    /**
     * The key which can perform update/delete operations on the token. If empty, the token can be perceived as immutable (not being able to be updated/deleted)
     */
    @Nullable
    public final Key adminKey;

    /**
     * The key which can grant or revoke KYC of an account for the token's transactions. If empty, KYC is not required, and KYC grant or revoke operations are not possible.
     */
    @Nullable
    public final Key kycKey;

    /**
     * The key which can freeze or unfreeze an account for token transactions. If empty, freezing is not possible
     */
    @Nullable
    public final Key freezeKey;

    /**
     * The key which can wipe token balance of an account. If empty, wipe is not possible
     */
    @Nullable
    public final Key wipeKey;

    /**
     * The key which can change the supply of a token. The key is used to sign Token Mint/Burn operations
     */
    @Nullable
    public final Key supplyKey;

    /**
     * The default Freeze status (not applicable, frozen or unfrozen) of Hedera accounts relative to this token. FreezeNotApplicable is returned if Token Freeze Key is empty. Frozen is returned if Token Freeze Key is set and defaultFreeze is set to true. Unfrozen is returned if Token Freeze Key is set and defaultFreeze is set to false
     */
    @Nullable
    public final Boolean defaultFreezeStatus;

    /**
     * The default KYC status (KycNotApplicable or Revoked) of Hedera accounts relative to this token. KycNotApplicable is returned if KYC key is not set, otherwise Revoked
     */
    @Nullable
    public final Boolean defaultKycStatus;

    /**
     * Specifies whether the token was deleted or not
     */
    public final boolean isDeleted;

    /**
     * An account which will be automatically charged to renew the token's expiration, at autoRenewPeriod interval
     */
    @Nullable
    public final AccountId autoRenewAccount;

    /**
     * The interval at which the auto-renew account will be charged to extend the token's expiry
     */
    @Nullable
    public final Duration autoRenewPeriod;

    /**
     * The epoch second at which the token will expire
     */
    @Nullable
    public final Instant expirationTime;

    /**
     * The memo associated with the token 
     */
    public final String tokenMemo;

    @Nullable
    public final CustomFeeList customFeeList;

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
        String tokenMemo,
        @Nullable CustomFeeList customFeeList
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
        this.customFeeList = customFeeList;
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
        return TokenInfo.fromProtobuf(response, null);
    }

    static TokenInfo fromProtobuf(TokenGetInfoResponse response, @Nullable NetworkName networkName) {
        var info = response.getTokenInfo();

        return new TokenInfo(
            TokenId.fromProtobuf(info.getTokenId(), networkName),
            info.getName(),
            info.getSymbol(),
            info.getDecimals(),
            info.getTotalSupply(),
            AccountId.fromProtobuf(info.getTreasury(), networkName),
            info.hasAdminKey() ? Key.fromProtobufKey(info.getAdminKey(), networkName) : null,
            info.hasKycKey() ? Key.fromProtobufKey(info.getKycKey(), networkName) : null,
            info.hasFreezeKey() ? Key.fromProtobufKey(info.getFreezeKey(), networkName) : null,
            info.hasWipeKey() ? Key.fromProtobufKey(info.getWipeKey(), networkName) : null,
            info.hasSupplyKey() ? Key.fromProtobufKey(info.getSupplyKey(), networkName) : null,
            freezeStatusFromProtobuf(info.getDefaultFreezeStatus()),
            kycStatusFromProtobuf(info.getDefaultKycStatus()),
            info.getDeleted(),
            info.hasAutoRenewAccount() ? AccountId.fromProtobuf(info.getAutoRenewAccount(), networkName) : null,
            info.hasAutoRenewPeriod() ? DurationConverter.fromProtobuf(info.getAutoRenewPeriod()) : null,
            info.hasExpiry() ? InstantConverter.fromProtobuf(info.getExpiry()) : null,
            info.getMemo(),
            info.hasCustomFees() ? CustomFeeList.fromProtobuf(info.getCustomFees()) : null
        );
    }

    public static TokenInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(TokenGetInfoResponse.parseFrom(bytes).toBuilder().build());
    }

    static TokenFreezeStatus freezeStatusToProtobuf(@Nullable Boolean freezeStatus) {
        return freezeStatus == null ? TokenFreezeStatus.FreezeNotApplicable : freezeStatus ? TokenFreezeStatus.Frozen : TokenFreezeStatus.Unfrozen;
    }

    static TokenKycStatus kycStatusToProtobuf(@Nullable Boolean kycStatus) {
        return kycStatus == null ? TokenKycStatus.KycNotApplicable : kycStatus ? TokenKycStatus.Granted : TokenKycStatus.Revoked;
    }

    TokenGetInfoResponse toProtobuf() {
        var tokenInfoBuilder = com.hedera.hashgraph.sdk.proto.TokenInfo.newBuilder()
            .setTokenId(tokenId.toProtobuf())
            .setName(name)
            .setSymbol(symbol)
            .setDecimals(decimals)
            .setTotalSupply(totalSupply)
            .setTreasury(treasuryAccountId.toProtobuf())
            .setDefaultFreezeStatus(freezeStatusToProtobuf(defaultFreezeStatus))
            .setDefaultKycStatus(kycStatusToProtobuf(defaultKycStatus))
            .setDeleted(isDeleted)
            .setMemo(tokenMemo);
        if(adminKey != null) {
            tokenInfoBuilder.setAdminKey(adminKey.toProtobufKey());
        }
        if(kycKey != null) {
            tokenInfoBuilder.setKycKey(kycKey.toProtobufKey());
        }
        if(freezeKey != null) {
            tokenInfoBuilder.setFreezeKey(freezeKey.toProtobufKey());
        }
        if(wipeKey != null) {
            tokenInfoBuilder.setWipeKey(wipeKey.toProtobufKey());
        }
        if(supplyKey != null) {
            tokenInfoBuilder.setSupplyKey(supplyKey.toProtobufKey());
        }
        if(autoRenewAccount != null) {
            tokenInfoBuilder.setAutoRenewAccount(autoRenewAccount.toProtobuf());
        }
        if(autoRenewPeriod != null) {
            tokenInfoBuilder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        }
        if(expirationTime != null) {
            tokenInfoBuilder.setExpiry(InstantConverter.toProtobuf(expirationTime));
        }
        return TokenGetInfoResponse.newBuilder().setTokenInfo(tokenInfoBuilder).build();
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
            .add("customFeeList", customFeeList)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
