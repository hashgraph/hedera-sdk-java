package com.hedera.hashgraph.sdk.token;

import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.TokenGetInfoResponse;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;

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
    public final PublicKey adminKey;

    /**
     * The key which can grant or revoke KYC of an account for the token's transactions. If empty, KYC is not required,
     * and KYC grant or revoke operations are not possible.
     */
    public final PublicKey kycKey;

    /**
     * The key which can freeze or unfreeze an account for token transactions. If empty, freezing is not possible
     */
    public final PublicKey freezeKey;

    /**
     * The key which can wipe token balance of an account. If empty, wipe is not possible
     */
    public final PublicKey wipeKey;

    /**
     * The key which can change the supply of a token. The key is used to sign Token Mint/Burn operations
     */
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
    public final boolean defaultFreezeStatus;

    /**
     * The default KYC status (KycNotApplicable or Revoked) of Hedera accounts relative to this token. KycNotApplicable
     * is returned if KYC key is not set, otherwise Revoked
     *      KycNotApplicable = null;
     *      Granted = false;
     *      Revoked = true;
     */
    @Nullable
    public final boolean defaultKycStatus;

    /**
     * Specifies whether the token was deleted or not
     */
    public final boolean isDeleted;

    /**
     * An account which will be automatically charged to renew the token's expiration, at autoRenewPeriod interval
     */
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

    TokenInfo(com.hedera.hashgraph.proto.TokenInfo info) {
        int defaultFreezeStatus = info.getDefaultFreezeStatus().getNumber();
        int defaultKycStatus = info.getDefaultKycStatus().getNumber();

        this.tokenId = new TokenId(info.getTokenId());
        this.name = info.getName();
        this.symbol = info.getSymbol();
        this.decimals = info.getDecimals();
        this.totalSupply = info.getTotalSupply();
        this.treasury = new AccountId(info.getTreasury());
        this.adminKey = Ed25519PublicKey.fromProtoKey(info.getAdminKey());
        this.kycKey = Ed25519PublicKey.fromProtoKey(info.getKycKey());
        this.freezeKey = Ed25519PublicKey.fromProtoKey(info.getFreezeKey());
        this.wipeKey = Ed25519PublicKey.fromProtoKey(info.getWipeKey());
        this.supplyKey = Ed25519PublicKey.fromProtoKey(info.getSupplyKey());
        this.defaultFreezeStatus = defaultFreezeStatus == 0 ? null : defaultFreezeStatus == 2;
        this.defaultKycStatus = defaultKycStatus == 0 ? null : defaultKycStatus == 2;
        this.isDeleted = info.getIsDeleted();
        this.autoRenewAccount = new AccountId(info.getAutoRenewAccount());
        this.autoRenewPeriod = Duration.ofSeconds(info.getAutoRenewPeriod());
        this.expiry = Instant.ofEpochSecond(info.getExpiry());
    }

    static TokenInfo fromResponse(Response response) {
        if (!response.hasTokenGetInfo()) {
            throw new IllegalArgumentException("query response was not `TokenGetInfoResponse`");
        }

        TokenGetInfoResponse infoResponse = response.getTokenGetInfo();

        return new TokenInfo(infoResponse.getTokenInfo());
    }
}
