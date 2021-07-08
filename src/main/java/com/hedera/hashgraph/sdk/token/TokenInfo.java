package com.hedera.hashgraph.sdk.token;

import com.google.common.annotations.Beta;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.TokenFreezeStatus;
import com.hedera.hashgraph.proto.TokenGetInfoResponse;
import com.hedera.hashgraph.proto.TokenKycStatus;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TokenInfo {
    /**
     * ID of the token instance
     */
    public final TokenId tokenId;

    /**
     * The name of the token. It is a string of ASCII only characters
     */
    public final String name;

    /**
     * The symbol of the token. It is a UTF-8 capitalized alphabetical string
     */
    public final String symbol;

    /**
     * The number of decimal places a token is divisible by
     */
    public final int decimals;

    /**
     * The total supply of tokens that are currently in circulation
     */
    public final long totalSupply;

    /**
     * The ID of the account which is set as Treasury
     */
    public final AccountId treasury;

    /**
     * The key which can perform update/delete operations on the token. If empty, the token can be perceived as
     * immutable (not being able to be updated/deleted)
     */
    @Nullable
    public final PublicKey adminKey;

    /**
     * The key which can grant or revoke KYC of an account for the token's transactions. If empty, KYC is not required,
     * and KYC grant or revoke operations are not possible.
     */
    @Nullable
    public final PublicKey kycKey;

    /**
     * The key which can freeze or unfreeze an account for token transactions. If empty, freezing is not possible
     */
    @Nullable
    public final PublicKey freezeKey;

    /**
     * The key which can wipe token balance of an account. If empty, wipe is not possible
     */
    @Nullable
    public final PublicKey wipeKey;

    /**
     * The key which can change the supply of a token. The key is used to sign Token Mint/Burn operations
     */
    @Nullable
    public final PublicKey supplyKey;

    /**
     * The default Freeze status (not applicable = null, frozen = false, or unfrozen = true) of Hedera accounts relative to this token.
     * FreezeNotApplicable is returned if Token Freeze Key is empty. Frozen is returned if Token Freeze Key is set and
     * defaultFreeze is set to true. Unfrozen is returned if Token Freeze Key is set and defaultFreeze is set to false
     *      FreezeNotApplicable = null;
     *      Frozen = false;
     *      Unfrozen = true;
     */
    @Nullable
    public final Boolean defaultFreezeStatus;

    /**
     * The default KYC status (KycNotApplicable or Revoked) of Hedera accounts relative to this token. KycNotApplicable
     * is returned if KYC key is not set, otherwise Revoked
     *      KycNotApplicable = null;
     *      Granted = false;
     *      Revoked = true;
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
    public final Duration autoRenewPeriod;

    /**
     * The epoch second at which the token will expire; if an auto-renew account and period are specified,
     * this is coerced to the current epoch second plus the autoRenewPeriod
     */
    public final Instant expiry;

    @Beta
    public final TokenType tokenType;

    @Beta
    public final TokenSupplyType supplyType;

    @Beta
    public final long maxSupply;

    @Beta
    @Nullable
    public final PublicKey feeScheduleKey;

    @Beta
    public final List<CustomFee> customFees;

    TokenInfo(com.hedera.hashgraph.proto.TokenInfo info) {
        TokenFreezeStatus defaultFreezeStatus = info.getDefaultFreezeStatus();
        TokenKycStatus defaultKycStatus = info.getDefaultKycStatus();

        this.tokenId = new TokenId(info.getTokenIdOrBuilder());
        this.name = info.getName();
        this.symbol = info.getSymbol();
        this.decimals = info.getDecimals();
        this.totalSupply = info.getTotalSupply();
        this.treasury = new AccountId(info.getTreasuryOrBuilder());
        this.adminKey = info.hasAdminKey() ? Ed25519PublicKey.fromProtoKey(info.getAdminKeyOrBuilder()) : null;
        this.kycKey = info.hasKycKey() ? Ed25519PublicKey.fromProtoKey(info.getKycKeyOrBuilder()) : null;
        this.freezeKey = info.hasFreezeKey() ? Ed25519PublicKey.fromProtoKey(info.getFreezeKeyOrBuilder()) : null;
        this.wipeKey = info.hasWipeKey() ? Ed25519PublicKey.fromProtoKey(info.getWipeKeyOrBuilder()) : null;
        this.supplyKey = info.hasSupplyKey() ? Ed25519PublicKey.fromProtoKey(info.getSupplyKeyOrBuilder()) : null;
        this.defaultFreezeStatus = defaultFreezeStatus == TokenFreezeStatus.FreezeNotApplicable ? null : defaultFreezeStatus == TokenFreezeStatus.Frozen;
        this.defaultKycStatus = defaultKycStatus == TokenKycStatus.KycNotApplicable ? null : defaultKycStatus == TokenKycStatus.Granted;
        this.isDeleted = info.getDeleted();
        this.autoRenewAccount = info.hasAutoRenewAccount() ? new AccountId(info.getAutoRenewAccountOrBuilder()) : null;
        this.autoRenewPeriod = DurationHelper.durationTo(info.getAutoRenewPeriod());
        this.expiry = TimestampHelper.timestampTo(info.getExpiry());
        this.tokenType = TokenType.valueOf(info.getTokenType());
        this.supplyType = TokenSupplyType.valueOf(info.getSupplyType());
        this.maxSupply = info.getMaxSupply();
        this.feeScheduleKey = info.hasFeeScheduleKey() ? Ed25519PublicKey.fromProtoKey(info.getFeeScheduleKey()) : null;

        List<CustomFee> customFees = new ArrayList<>();
        for (com.hedera.hashgraph.proto.CustomFee fee : info.getCustomFeesList()) {
            customFees.add(CustomFee.fromProto(fee));
        }
        this.customFees = customFees;
    }

    static TokenInfo fromResponse(Response response) {
        if (!response.hasTokenGetInfo()) {
            throw new IllegalArgumentException("query response was not `TokenGetInfoResponse`");
        }

        TokenGetInfoResponse infoResponse = response.getTokenGetInfo();

        return new TokenInfo(infoResponse.getTokenInfo());
    }
}
