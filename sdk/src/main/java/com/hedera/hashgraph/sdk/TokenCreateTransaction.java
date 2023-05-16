/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.grpc.MethodDescriptor;
import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Create a new fungible or non-fungible token (NFT) on the Hedera network.
 * After you submit the transaction to the Hedera network, you can obtain the
 * new token ID by requesting the receipt. Smart contracts cannot access or
 * transfer HTS tokens at this time.
 *
 * NFTs
 *
 * For non-fungible tokens, the token ID represents a NFT class. Once the token
 * is created, you will have to mint each NFT using the token mint operation.
 *
 * See <a href="https://docs.hedera.com/guides/docs/sdks/tokens/define-a-token">Hedera Documentation</a>
 */
public class TokenCreateTransaction extends Transaction<TokenCreateTransaction> {
    private List<CustomFee> customFees = new ArrayList<>();
    /**
     * The account which will act as a treasury for the token. This account
     * will receive the specified initial supply and any additional tokens
     * that are minted.  If tokens are burned, the supply will be decreased
     * from the treasury account.
     */
    @Nullable
    private AccountId treasuryAccountId = null;
    /**
     * An account which will be automatically charged to renew the token's
     * expiration, at autoRenewPeriod interval. This key is required to
     * sign the transaction if present. This is not currently enabled.
     */
    @Nullable
    private AccountId autoRenewAccountId = null;
    /**
     * Set the publicly visible name of the token. The token name is specified
     * as a string of UTF-8 characters in Unicode. UTF-8 encoding of this
     * Unicode cannot contain the 0 byte (NUL). The token name is not unique.
     * Maximum of 100 characters.
     */
    private String tokenName = "";
    /**
     * The publicly visible token symbol. Set the publicly visible name of the
     * token. The token symbol is specified as a string of UTF-8 characters in
     * Unicode. UTF-8 encoding of this Unicode cannot contain the 0 byte (NUL).
     * The token symbol is not unique. Maximum of 100 characters.
     */
    private String tokenSymbol = "";
    /**
     * The number of decimal places a token is divisible by. This field can
     * never be changed.
     */
    private int decimals = 0;
    /**
     * Specifies the initial supply of fungible tokens to be put in
     * circulation. The initial supply is sent to the Treasury Account.
     * The maximum supply of tokens is 9,223,372,036,854,775,807(2^63-1)
     * tokens and is in the lowest denomination possible. For creating an
     * NFT, you must set the initial supply to 0.
     */
    private long initialSupply = 0;
    /**
     * The key which can perform token update and token delete operations on
     * the token.The admin key has the authority to change the freeze key,
     * wipe key, and KYC key. It can also update the treasury account of the
     * token.  If empty, the token can be perceived as immutable (not being
     * able to be updated/deleted). (KYC - Know Your Customer)
     */
    @Nullable
    private Key adminKey = null;
    /**
     * The key which can grant or revoke KYC of an account for the token's
     * transactions. If empty, KYC is not required, and KYC grant or revoke
     * operations are not possible.
     */
    @Nullable
    private Key kycKey = null;
    /**
     * The key which can sign to freeze or unfreeze an account for token
     * transactions. If empty, freezing is not possible.
     */
    @Nullable
    private Key freezeKey = null;
    /**
     * The key which can wipe the token balance of an account. If empty,
     * wipe is not possible.
     */
    @Nullable
    private Key wipeKey = null;
    /**
     * The key which can change the total supply of a token. This key is
     * used to authorize token mint and burn transactions. If this is left
     * empty, minting/burning tokens is not possible.
     */
    @Nullable
    private Key supplyKey = null;
    /**
     * The key that can change the token's  schedule. A custom fee schedule
     * token without a fee schedule key is immutable.
     */
    @Nullable
    private Key feeScheduleKey = null;
    /**
     * The key that has the authority to pause or unpause a token. Pausing
     * a token prevents the token from participating in all transactions.
     */
    @Nullable
    private Key pauseKey = null;
    /**
     * The default Freeze status (frozen or unfrozen) of Hedera accounts
     * relative to this token. If true, an account must be unfrozen before
     * it can receive the token.
     */
    private boolean freezeDefault = false;
    /**
     * The epoch second at which the token should expire; if an auto-renew
     * account and period are specified, this is coerced to the current
     * epoch second plus the autoRenewPeriod. The default expiration time
     * is 90 days.
     */
    @Nullable
    private Instant expirationTime = null;
    /**
     * The interval at which the auto-renew account will be charged to
     * extend the token's expiry. The default auto-renew period is
     * 131,500 minutes. This is not currently enabled.
     */
    @Nullable
    private Duration autoRenewPeriod = null;
    /**
     * A short publicly visible memo about the token.
     */
    private String tokenMemo = "";
    /**
     * The type of token to create. Either fungible or non-fungible.
     */
    private TokenType tokenType = TokenType.FUNGIBLE_COMMON;
    /**
     * Specifies the token supply type. Defaults to INFINITE.
     */
    private TokenSupplyType tokenSupplyType = TokenSupplyType.INFINITE;
    /**
     * For tokens of type FUNGIBLE_COMMON - the maximum number of tokens that
     * can be in circulation.
     *
     * For tokens of type NON_FUNGIBLE_UNIQUE - the maximum number of NFTs
     * (serial numbers) that can be minted. This field can never be changed.
     *
     * You must set the token supply type to FINITE if you set this field.
     */
    private long maxSupply = 0;

    /**
     * Constructor.
     */
    public TokenCreateTransaction() {
        setAutoRenewPeriod(DEFAULT_AUTO_RENEW_PERIOD);
        defaultMaxTransactionFee = new Hbar(40);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenCreateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the token name.
     *
     * @return                          the token name
     */
    @Nullable
    public String getTokenName() {
        return tokenName;
    }

    /**
     * Assign the token's name 100 bytes max.
     *
     * @param name                      the token name
     * @return {@code this}
     */
    public TokenCreateTransaction setTokenName(String name) {
        Objects.requireNonNull(name);
        requireNotFrozen();
        tokenName = name;
        return this;
    }

    /**
     * Extract the token symbol.
     *
     * @return                          the token symbol
     */
    public String getTokenSymbol() {
        return tokenSymbol;
    }

    /**
     * Assign the token's symbol 100 bytes max.
     *
     * @param symbol                    the token symbol
     * @return {@code this}
     */
    public TokenCreateTransaction setTokenSymbol(String symbol) {
        Objects.requireNonNull(symbol);
        requireNotFrozen();
        tokenSymbol = symbol;
        return this;
    }

    /**
     * Extract the decimals.
     *
     * @return                          the decimals
     */
    public int getDecimals() {
        return decimals;
    }

    /**
     * Assign the number of decimal places a token is divisible by.
     *
     * This field can never be changed.
     *
     * @param decimals                  the number of decimals
     * @return {@code this}
     */
    public TokenCreateTransaction setDecimals(@Nonnegative int decimals) {
        requireNotFrozen();
        this.decimals = decimals;
        return this;
    }

    /**
     * Extract the initial supply of tokens.
     *
     * @return                          the initial supply of tokens
     */
    public long getInitialSupply() {
        return initialSupply;
    }

    /**
     * Assign the initial supply of tokens.
     *
     * @param initialSupply             the initial supply of tokens
     * @return {@code this}
     */
    public TokenCreateTransaction setInitialSupply(@Nonnegative long initialSupply) {
        requireNotFrozen();
        this.initialSupply = initialSupply;
        return this;
    }

    /**
     * Extract the treasury account id.
     *
     * @return                          the treasury account id
     */
    @Nullable
    public AccountId getTreasuryAccountId() {
        return treasuryAccountId;
    }

    /**
     * Assign the treasury account id.
     *
     * @param accountId                 the treasury account id
     * @return {@code this}
     */
    public TokenCreateTransaction setTreasuryAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.treasuryAccountId = accountId;
        return this;
    }

    /**
     * Extract the admin key.
     *
     * @return                          the admin key
     */
    @Nullable
    public Key getAdminKey() {
        return adminKey;
    }

    /**
     * Assign the admin key.
     *
     * @param key                       the admin key
     * @return {@code this}
     */
    public TokenCreateTransaction setAdminKey(Key key) {
        requireNotFrozen();
        Objects.requireNonNull(key);
        adminKey = key;
        return this;
    }

    /**
     * Extract the know your customer key.
     *
     * @return                          the know your customer key
     */
    @Nullable
    public Key getKycKey() {
        return kycKey;
    }

    /**
     * Assign the kyc key.
     *
     * @param key                       the know your customer key
     * @return {@code this}
     */
    public TokenCreateTransaction setKycKey(Key key) {
        requireNotFrozen();
        kycKey = key;
        return this;
    }

    /**
     * Extract the freeze key.
     *
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
    public TokenCreateTransaction setFreezeKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        freezeKey = key;
        return this;
    }

    /**
     * Extract the wipe key.
     *
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
    public TokenCreateTransaction setWipeKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        wipeKey = key;
        return this;
    }

    /**
     * Extract the supply key.
     *
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
    public TokenCreateTransaction setSupplyKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        supplyKey = key;
        return this;
    }

    /**
     * Extract the fee schedule key.
     *
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
    public TokenCreateTransaction setFeeScheduleKey(Key key) {
        requireNotFrozen();
        Objects.requireNonNull(key);
        feeScheduleKey = key;
        return this;
    }

    /**
     * Extract the pause key.
     *
     * @return                          the pause key
     */
    @Nullable
    public Key getPauseKey() {
        return pauseKey;
    }

    /**
     * Assign the pause key.
     *
      * @param key                      the pause key
     * @return {@code this}
     */
    public TokenCreateTransaction setPauseKey(Key key) {
        requireNotFrozen();
        Objects.requireNonNull(key);
        pauseKey = key;
        return this;
    }

    /**
     * Extract the freeze default.
     *
     * @return                          the freeze default
     */
    public boolean getFreezeDefault() {
        return freezeDefault;
    }

    /**
     * Assign the freeze default.
     *
     * @param freezeDefault             the freeze default
     * @return {@code this}
     */
    public TokenCreateTransaction setFreezeDefault(boolean freezeDefault) {
        requireNotFrozen();
        this.freezeDefault = freezeDefault;
        return this;
    }

    /**
     * Extract the expiration time.
     *
     * @return                          the expiration time
     */
    @Nullable
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "An Instant can't actually be mutated"
    )
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * Assign the expiration time.
     *
     * @param expirationTime            the expiration time
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "An Instant can't actually be mutated"
    )
    public TokenCreateTransaction setExpirationTime(Instant expirationTime) {
        Objects.requireNonNull(expirationTime);
        requireNotFrozen();
        autoRenewPeriod = null;
        this.expirationTime = expirationTime;
        return this;
    }

    /**
     * Extract the auto renew account id.
     *
     * @return                          the auto renew account id
     */
    @Nullable
    public AccountId getAutoRenewAccountId() {
        return autoRenewAccountId;
    }

    /**
     * Assign the auto renew account id.
     *
     * @param accountId                 the auto renew account id
     * @return {@code this}
     */
    public TokenCreateTransaction setAutoRenewAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.autoRenewAccountId = accountId;
        return this;
    }

    /**
     * Extract the auto renew period.
     *
     * @return                          the auto renew period
     */
    @Nullable
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP",
        justification = "A Duration can't actually be mutated"
    )
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Assign the auto renew period.
     *
     * @param period                    the auto renew period
     * @return {@code this}
     */
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "A Duration can't actually be mutated"
    )
    public TokenCreateTransaction setAutoRenewPeriod(Duration period) {
        Objects.requireNonNull(period);
        requireNotFrozen();
        autoRenewPeriod = period;
        return this;
    }

    /**
     * Extract the token's memo 100 bytes max.
     *
     * @return                          the token's memo 100 bytes max
     */
    public String getTokenMemo() {
        return tokenMemo;
    }

    /**
     * Assign the token's memo.
     *
     * @param memo                      the token's memo 100 bytes max
     * @return {@code this}
     */
    public TokenCreateTransaction setTokenMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        tokenMemo = memo;
        return this;
    }

    /**
     * Extract the custom fees.
     *
     * @return                          the custom fees
     */
    @Nullable
    public List<CustomFee> getCustomFees() {
        return CustomFee.deepCloneList(customFees);
    }

    /**
     * Assign the custom fees.
     *
     * @param customFees                the custom fees
     * @return {@code this}
     */
    public TokenCreateTransaction setCustomFees(List<CustomFee> customFees) {
        requireNotFrozen();
        this.customFees = CustomFee.deepCloneList(customFees);
        return this;
    }

    /**
     * Extract the token type.
     *
     * @return                          the token type
     */
    public TokenType getTokenType() {
        return tokenType;
    }

    /**
     * Assign the token type.
     *
     * @param tokenType                 the token type
     * @return {@code this}
     */
    public TokenCreateTransaction setTokenType(TokenType tokenType) {
        requireNotFrozen();
        Objects.requireNonNull(tokenType);
        this.tokenType = tokenType;
        return this;
    }

    /**
     * Extract the supply type.
     *
     * @return                          the supply type
     */
    public TokenSupplyType getSupplyType() {
        return tokenSupplyType;
    }

    /**
     * Assign the supply type.
     *
      * @param supplyType               the supply type
     * @return {@code this}
     */
    public TokenCreateTransaction setSupplyType(TokenSupplyType supplyType) {
        requireNotFrozen();
        Objects.requireNonNull(supplyType);
        tokenSupplyType = supplyType;
        return this;
    }

    /**
     * Extract the max supply of tokens.
     *
     * @return                          the max supply of tokens
     */
    public long getMaxSupply() {
        return maxSupply;
    }

    /**
     * Assign the max supply of tokens.
     *
     * @param maxSupply                 the max supply of tokens
     * @return {@code this}
     */
    public TokenCreateTransaction setMaxSupply(@Nonnegative long maxSupply) {
        requireNotFrozen();
        this.maxSupply = maxSupply;
        return this;
    }

    @Override
    public TokenCreateTransaction freezeWith(@Nullable Client client) {
        if (
            autoRenewPeriod != null &&
                autoRenewAccountId == null &&
                client != null &&
                client.getOperatorAccountId() != null
        ) {
            autoRenewAccountId = client.getOperatorAccountId();
        }

        return super.freezeWith(client);
    }

    /**
     * Build the transaction body.
     *
     * @return {@link com.hedera.hashgraph.sdk.proto.TokenCreateTransactionBody}
     */
    TokenCreateTransactionBody.Builder build() {
        var builder = TokenCreateTransactionBody.newBuilder();
        if (treasuryAccountId != null) {
            builder.setTreasury(treasuryAccountId.toProtobuf());
        }

        if (autoRenewAccountId != null) {
            builder.setAutoRenewAccount(autoRenewAccountId.toProtobuf());
        }
        builder.setName(tokenName);
        builder.setSymbol(tokenSymbol);
        builder.setDecimals(decimals);
        builder.setInitialSupply(initialSupply);
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
        builder.setFreezeDefault(freezeDefault);
        if (expirationTime != null) {
            builder.setExpiry(InstantConverter.toProtobuf(expirationTime));
        }
        if (autoRenewPeriod != null) {
            builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        }
        builder.setMemo(tokenMemo);
        builder.setTokenType(tokenType.code);
        builder.setSupplyType(tokenSupplyType.code);
        builder.setMaxSupply(maxSupply);

        for (var fee : customFees) {
            builder.addCustomFees(fee.toProtobuf());
        }

        return builder;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenCreation();
        if (body.hasTreasury()) {
            treasuryAccountId = AccountId.fromProtobuf(body.getTreasury());
        }
        if (body.hasAutoRenewAccount()) {
            autoRenewAccountId = AccountId.fromProtobuf(body.getAutoRenewAccount());
        }
        tokenName = body.getName();
        tokenSymbol = body.getSymbol();
        decimals = body.getDecimals();
        initialSupply = body.getInitialSupply();
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
        freezeDefault = body.getFreezeDefault();
        if (body.hasExpiry()) {
            expirationTime = InstantConverter.fromProtobuf(body.getExpiry());
        }
        if (body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }
        tokenMemo = body.getMemo();
        tokenType = TokenType.valueOf(body.getTokenType());
        tokenSupplyType = TokenSupplyType.valueOf(body.getSupplyType());
        maxSupply = body.getMaxSupply();

        for (var fee : body.getCustomFeesList()) {
            customFees.add(CustomFee.fromProtobuf(fee));
        }
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        for (var fee : customFees) {
            fee.validateChecksums(client);
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
        return TokenServiceGrpc.getCreateTokenMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenCreation(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenCreation(build());
    }
}
