package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TokenUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * A transaction that updates the properties of an existing token. The admin
 * key must sign this transaction to update any of the token properties. The
 * admin key can update exisitng keys, but cannot add new keys if they were
 * not set during the creation of the token. If no value is given for a
 * field, that field is left unchanged.
 *
 * For an immutable token (that is, a token created without an admin key),
 * only the expiry may be updated. Setting any other field, in that case,
 * will cause the transaction status to resolve to TOKEN_IS_IMMUTABlE.
 *
 * {@link https://docs.hedera.com/guides/docs/sdks/tokens/update-a-token}
 */
public class TokenUpdateTransaction extends Transaction<TokenUpdateTransaction> {
    /**
     * The token's id
     */
    @Nullable
    private TokenId tokenId = null;
    /**
     * The new treasury account of the token. If the provided treasury
     * account is not existing or deleted, the response will be
     * INVALID_TREASURY_ACCOUNT_FOR_TOKEN. If successful, the Token balance
     * held in the previous Treasury Account is transferred to the new one.
     */
    @Nullable
    private AccountId treasuryAccountId = null;
    /**
     * The new account which will be automatically charged to renew the
     * token's expiration, at autoRenewPeriod interval.
     */
    @Nullable
    private AccountId autoRenewAccountId = null;
    /**
     * The new name of the token. The token name is specified as a string of
     * UTF-8 characters in Unicode. UTF-8 encoding of this Unicode cannot
     * contain the 0 byte (NUL). Is not required to be unique.
     */
    private String tokenName = "";
    /**
     * The new symbol of the token. The token symbol is specified as a string
     * of UTF-8 characters in Unicode. UTF-8 encoding of this Unicode cannot
     * contain the 0 byte (NUL). Is not required to be unique.
     */
    private String tokenSymbol = "";
    /**
     * The new admin key of the token. If the token is immutable (no Admin
     * Key was assigned during token creation), the transaction will resolve
     * to TOKEN_IS_IMMUTABlE. Admin keys cannot update to add new keys that
     * were not specified during the creation of the token.
     */
    @Nullable
    private Key adminKey = null;
    /**
     * The new KYC key of the token. If the token does not have currently
     * a KYC key, the transaction will resolve to TOKEN_HAS_NO_KYC_KEY.
     */
    @Nullable
    private Key kycKey = null;
    /**
     * The new freeze key of the token. If the token does not have currently
     * a freeze key, the transaction will resolve to TOKEN_HAS_NO_FREEZE_KEY.
     */
    @Nullable
    private Key freezeKey = null;
    /**
     * The new wipe key of the token. If the token does not have currently
     * a wipe key, the transaction will resolve to TOKEN_HAS_NO_WIPE_KEY.
     */
    @Nullable
    private Key wipeKey = null;
    /**
     * The new supply key of the token. If the token does not have currently
     * a supply key, the transaction will resolve to TOKEN_HAS_NO_SUPPLY_KEY.
     */
    @Nullable
    private Key supplyKey = null;
    /**
     * If set, the new key to use to update the token's custom fee schedule;
     * if the token does not currently have this key, transaction will
     * resolve to TOKEN_HAS_NO_FEE_SCHEDULE_KEY
     */
    @Nullable
    private Key feeScheduleKey = null;
    /**
     * Update the token's existing pause key. The pause key has the ability
     * to pause or unpause a token.
     */
    @Nullable
    private Key pauseKey = null;
    /**
     * The new expiry time of the token. Expiry can be updated even if the
     * admin key is not set. If the provided expiry is earlier than the
     * current token expiry, the transaction will resolve to
     * INVALID_EXPIRATION_TIME.
     */
    @Nullable
    private Instant expirationTime = null;
    /**
     * The new interval at which the auto-renew account will be charged to
     * extend the token's expiry.
     *
     * The default auto-renew period is 131,500 minutes.
     */
    @Nullable
    private Duration autoRenewPeriod = null;
    /**
     * Short publicly visible memo about the token. No guarantee of
     * uniqueness. (100 characters max)
     */
    @Nullable
    private String tokenMemo = null;

    /**
     * Constructor.
     */
    public TokenUpdateTransaction() {
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException
     */
    TokenUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * @return                          the token id
     */
    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    /**
     * Assign the token id.
     *
     * @param tokenId                   the token id
     * @return
     */
    public TokenUpdateTransaction setTokenId(TokenId tokenId) {
        requireNotFrozen();
        Objects.requireNonNull(tokenId);
        this.tokenId = tokenId;
        return this;
    }

    /**
     * @return                          the token name
     */
    @Nullable
    public String getTokenName() {
        return tokenName;
    }

    /**
     * Assign the token name.
     *
     * @param name                      the token name
     * @return {@code this}
     */
    public TokenUpdateTransaction setTokenName(String name) {
        Objects.requireNonNull(name);
        requireNotFrozen();
        tokenName = name;
        return this;
    }

    /**
     * @return                          the token symbol
     */
    public String getTokenSymbol() {
        return tokenSymbol;
    }

    /**
     * Assign the token symbol.
     *
     * @param symbol                    the token symbol
     * @return {@code this}
     */
    public TokenUpdateTransaction setTokenSymbol(String symbol) {
        Objects.requireNonNull(symbol);
        requireNotFrozen();
        tokenSymbol = symbol;
        return this;
    }

    /**
     * @return                          the treasury account id
     */
    @Nullable
    public AccountId getTreasuryAccountId() {
        return treasuryAccountId;
    }

    /**
     * Assign the account id.
     *
     * @param accountId                 the account id
     * @return {@code this}
     */
    public TokenUpdateTransaction setTreasuryAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.treasuryAccountId = accountId;
        return this;
    }

    /**
     * @return                          the admin key
     */
    @Nullable
    public Key getAdminKey() {
        return adminKey;
    }

    /**
     * Assign the key.
     *
     * @param key                       the key
     * @return {@code this}
     */
    public TokenUpdateTransaction setAdminKey(Key key) {
        requireNotFrozen();
        Objects.requireNonNull(key);
        adminKey = key;
        return this;
    }

    /**
     * @return                          the kyc key
     */
    @Nullable
    public Key getKycKey() {
        return kycKey;
    }

    /**
     * Assign the kyc key.
     *
     * @param key                       the kyc key
     * @return {@code this}
     */
    public TokenUpdateTransaction setKycKey(Key key) {
        requireNotFrozen();
        kycKey = key;
        return this;
    }

    /**
     * @return                          the freeze key
     */
    @Nullable
    public Key getFreezeKey() {
        return freezeKey;
    }

    /**
     * Assign the freeze key.
     *
     * @param key                       the freeze key
     * @return {@code this}
     */
    public TokenUpdateTransaction setFreezeKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        freezeKey = key;
        return this;
    }

    /**
     * @return                          the wipe key
     */
    @Nullable
    public Key getWipeKey() {
        return wipeKey;
    }

    /**
     * Assign the wipe key.
     *
     * @param key                       the wipe key
     * @return {@code this}
     */
    public TokenUpdateTransaction setWipeKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        wipeKey = key;
        return this;
    }

    /**
     * @return                          the supply key
     */
    @Nullable
    public Key getSupplyKey() {
        return supplyKey;
    }

    /**
     * Assign the supply key.
     *
     * @param key                       the supply key
     * @return {@code this}
     */
    public TokenUpdateTransaction setSupplyKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        supplyKey = key;
        return this;
    }

    /**
     * @return                          the fee schedule key
     */
    @Nullable
    public Key getFeeScheduleKey() {
        return feeScheduleKey;
    }

    /**
     * Assign the fee schedule key.
     *
     * @param key                       the fee schedule key
     * @return {@code this}
     */
    public TokenUpdateTransaction setFeeScheduleKey(Key key) {
        requireNotFrozen();
        Objects.requireNonNull(key);
        feeScheduleKey = key;
        return this;
    }

    /**
     * @return                          the pause key
     */
    @Nullable
    public Key getPauseKey() {
        return pauseKey;
    }

    /**
     * Assign the pause key.
     *
     * @param key                       the pause key
     * @return {@code this}
     */
    public TokenUpdateTransaction setPauseKey(Key key) {
        requireNotFrozen();
        Objects.requireNonNull(key);
        pauseKey = key;
        return this;
    }

    /**
     * @return                          the expiration time
     */
    @Nullable
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * Assign the expiration time.
     *
     * @param expirationTime            the expiration time
     * @return {@code this}
     */
    public TokenUpdateTransaction setExpirationTime(Instant expirationTime) {
        Objects.requireNonNull(expirationTime);
        requireNotFrozen();
        autoRenewPeriod = null;
        this.expirationTime = expirationTime;
        return this;
    }

    /**
     * @return                          the auto renew account id
     */
    @Nullable
    public AccountId getAutoRenewAccountId() {
        return autoRenewAccountId;
    }

    /**
     * Assign the auto renew account id.
     *
     * @param accountId                 the account id
     * @return {@code this}
     */
    public TokenUpdateTransaction setAutoRenewAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.autoRenewAccountId = accountId;
        return this;
    }

    /**
     * @return                          the auto renew period
     */
    @Nullable
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Assign the auto renew period.
     *
     * @param period                    the auto renew period
     * @return {@code this}
     */
    public TokenUpdateTransaction setAutoRenewPeriod(Duration period) {
        Objects.requireNonNull(period);
        requireNotFrozen();
        autoRenewPeriod = period;
        return this;
    }

    /**
     * @return                          the token memo
     */
    @Nullable
    public String getTokenMemo() {
        return tokenMemo;
    }

    /**
     * Assign the token memo.
     *
     * @param memo                      the token memo 100 bytes max
     * @return {@code this}
     */
    public TokenUpdateTransaction setTokenMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        tokenMemo = memo;
        return this;
    }

    /**
     * Remove the token memo.
     *
     * @return {@code this}
     */
    public TokenUpdateTransaction clearMemo() {
        requireNotFrozen();
        tokenMemo = "";
        return this;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenUpdate();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }
        if (body.hasTreasury()) {
            treasuryAccountId = AccountId.fromProtobuf(body.getTreasury());
        }
        if (body.hasAutoRenewAccount()) {
            autoRenewAccountId = AccountId.fromProtobuf(body.getAutoRenewAccount());
        }
        tokenName = body.getName();
        tokenSymbol = body.getSymbol();
        if (body.hasAdminKey()) {
            adminKey = Key.fromProtobufKey(body.getAdminKey());
        }
        if (body.hasKycKey()) {
            kycKey = Key.fromProtobufKey(body.getKycKey());
        }
        if (body.hasFreezeKey()) {
            freezeKey = Key.fromProtobufKey(body.getFreezeKey());
        }
        if (body.hasWipeKey()) {
            wipeKey = Key.fromProtobufKey(body.getWipeKey());
        }
        if (body.hasSupplyKey()) {
            supplyKey = Key.fromProtobufKey(body.getSupplyKey());
        }
        if (body.hasFeeScheduleKey()) {
            feeScheduleKey = Key.fromProtobufKey(body.getFeeScheduleKey());
        }
        if (body.hasPauseKey()) {
            pauseKey = Key.fromProtobufKey(body.getPauseKey());
        }
        if (body.hasExpiry()) {
            expirationTime = InstantConverter.fromProtobuf(body.getExpiry());
        }
        if (body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }
        if (body.hasMemo()) {
            tokenMemo = body.getMemo().getValue();
        }
    }

    /**
     * Build the transaction body.
     *
     * @return {@code {@link
     *         com.hedera.hashgraph.sdk.proto.TokenUpdateTransactionBody}}
     */
    TokenUpdateTransactionBody.Builder build() {
        var builder = TokenUpdateTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }
        if (treasuryAccountId != null) {
            builder.setTreasury(treasuryAccountId.toProtobuf());
        }

        if (autoRenewAccountId != null) {
            builder.setAutoRenewAccount(autoRenewAccountId.toProtobuf());
        }
        builder.setName(tokenName);
        builder.setSymbol(tokenSymbol);
        if (adminKey != null) {
            builder.setAdminKey(adminKey.toProtobufKey());
        }
        if (kycKey != null) {
            builder.setKycKey(kycKey.toProtobufKey());
        }
        if (freezeKey != null) {
            builder.setFreezeKey(freezeKey.toProtobufKey());
        }
        if (wipeKey != null) {
            builder.setWipeKey(wipeKey.toProtobufKey());
        }
        if (supplyKey != null) {
            builder.setSupplyKey(supplyKey.toProtobufKey());
        }
        if (feeScheduleKey != null) {
            builder.setFeeScheduleKey(feeScheduleKey.toProtobufKey());
        }
        if (pauseKey != null) {
            builder.setPauseKey(pauseKey.toProtobufKey());
        }
        if (expirationTime != null) {
            builder.setExpiry(InstantConverter.toProtobuf(expirationTime));
        }
        if (autoRenewPeriod != null) {
            builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        }
        if (tokenMemo != null) {
            builder.setMemo(StringValue.of(tokenMemo));
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }

        if (treasuryAccountId != null) {
            treasuryAccountId.validateChecksum(client);
        }

        if (autoRenewAccountId != null) {
            autoRenewAccountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getUpdateTokenMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenUpdate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenUpdate(build());
    }
}
