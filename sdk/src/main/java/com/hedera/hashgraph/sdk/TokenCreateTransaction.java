package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;

public class TokenCreateTransaction extends Transaction<TokenCreateTransaction> {
    private List<CustomFee> customFees = new ArrayList<>();
    @Nullable
    private AccountId treasuryAccountId = null;
    @Nullable
    private AccountId autoRenewAccountId = null;
    private String tokenName = "";
    private String tokenSymbol = "";
    private int decimals = 0;
    private long initialSupply = 0;
    @Nullable
    private Key adminKey = null;
    @Nullable
    private Key kycKey = null;
    @Nullable
    private Key freezeKey = null;
    @Nullable
    private Key wipeKey = null;
    @Nullable
    private Key supplyKey = null;
    @Nullable
    private Key feeScheduleKey = null;
    private boolean freezeDefault = false;
    @Nullable
    private Instant expirationTime = null;
    @Nullable
    private Duration autoRenewPeriod = null;
    private String tokenMemo = "";
    private TokenType tokenType = TokenType.FUNGIBLE_COMMON;
    private TokenSupplyType tokenSupplyType = TokenSupplyType.INFINITE;
    private long maxSupply = 0;

    public TokenCreateTransaction() {
        setAutoRenewPeriod(DEFAULT_AUTO_RENEW_PERIOD);
        setMaxTransactionFee(new Hbar(30));
    }

    TokenCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
    }

    TokenCreateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
    }

    @Nullable
    public String getTokenName() {
        return tokenName;
    }

    public TokenCreateTransaction setTokenName(String name) {
        Objects.requireNonNull(name);
        requireNotFrozen();
        tokenName = name;
        return this;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public TokenCreateTransaction setTokenSymbol(String symbol) {
        Objects.requireNonNull(symbol);
        requireNotFrozen();
        tokenSymbol = symbol;
        return this;
    }

    public int getDecimals() {
        return decimals;
    }

    public TokenCreateTransaction setDecimals(int decimals) {
        requireNotFrozen();
        this.decimals = decimals;
        return this;
    }

    public long getInitialSupply() {
        return initialSupply;
    }

    public TokenCreateTransaction setInitialSupply(long initialSupply) {
        requireNotFrozen();
        this.initialSupply = initialSupply;
        return this;
    }

    @Nullable
    public AccountId getTreasuryAccountId() {
        return treasuryAccountId;
    }

    public TokenCreateTransaction setTreasuryAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.treasuryAccountId = accountId;
        return this;
    }

    @Nullable
    public Key getAdminKey() {
        return adminKey;
    }

    public TokenCreateTransaction setAdminKey(Key key) {
        requireNotFrozen();
        Objects.requireNonNull(key);
        adminKey = key;
        return this;
    }

    @Nullable
    public Key getKycKey() {
        return kycKey;
    }

    public TokenCreateTransaction setKycKey(Key key) {
        requireNotFrozen();
        kycKey = key;
        return this;
    }

    @Nullable
    public Key getFreezeKey() {
        return freezeKey;
    }

    public TokenCreateTransaction setFreezeKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        freezeKey = key;
        return this;
    }

    @Nullable
    public Key getWipeKey() {
        return wipeKey;
    }

    public TokenCreateTransaction setWipeKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        wipeKey = key;
        return this;
    }

    @Nullable
    public Key getSupplyKey() {
        return supplyKey;
    }

    public TokenCreateTransaction setSupplyKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        supplyKey = key;
        return this;
    }

    @Nullable
    public Key getFeeScheduleKey() {
        return feeScheduleKey;
    }

    public TokenCreateTransaction setFeeScheduleKey(Key key) {
        requireNotFrozen();
        Objects.requireNonNull(key);
        feeScheduleKey = key;
        return this;
    }

    public boolean getFreezeDefault() {
        return freezeDefault;
    }

    public TokenCreateTransaction setFreezeDefault(boolean freezeDefault) {
        requireNotFrozen();
        this.freezeDefault = freezeDefault;
        return this;
    }

    @Nullable
    public Instant getExpirationTime() {
        return expirationTime;
    }

    public TokenCreateTransaction setExpirationTime(Instant expirationTime) {
        Objects.requireNonNull(expirationTime);
        requireNotFrozen();
        autoRenewPeriod = null;
        this.expirationTime = expirationTime;
        return this;
    }

    @Nullable
    public AccountId getAutoRenewAccountId() {
        return autoRenewAccountId;
    }

    public TokenCreateTransaction setAutoRenewAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.autoRenewAccountId = accountId;
        return this;
    }

    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    public TokenCreateTransaction setAutoRenewPeriod(Duration period) {
        Objects.requireNonNull(period);
        requireNotFrozen();
        autoRenewPeriod = period;
        return this;
    }

    public String getTokenMemo() {
        return tokenMemo;
    }

    public TokenCreateTransaction setTokenMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        tokenMemo = memo;
        return this;
    }

    public TokenCreateTransaction setCustomFees(List<CustomFee> customFees) {
        requireNotFrozen();
        this.customFees = customFees;
        return this;
    }

    @Nullable
    public List<CustomFee> getCustomFees() {
        return CustomFee.deepCloneList(customFees);
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public TokenCreateTransaction setTokenType(TokenType tokenType) {
        requireNotFrozen();
        Objects.requireNonNull(tokenType);
        this.tokenType = tokenType;
        return this;
    }

    public TokenSupplyType getSupplyType() {
        return tokenSupplyType;
    }

    public TokenCreateTransaction setSupplyType(TokenSupplyType supplyType) {
        requireNotFrozen();
        Objects.requireNonNull(supplyType);
        tokenSupplyType = supplyType;
        return this;
    }

    public long getMaxSupply() {
        return maxSupply;
    }

    public TokenCreateTransaction setMaxSupply(long maxSupply) {
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
        if(adminKey != null) {
            builder.setAdminKey(adminKey.toProtobufKey());
        }
        if(kycKey != null) {
            builder.setKycKey(kycKey.toProtobufKey());
        }
        if(freezeKey != null) {
            builder.setFreezeKey(freezeKey.toProtobufKey());
        }
        if(wipeKey != null) {
            builder.setWipeKey(wipeKey.toProtobufKey());
        }
        if(supplyKey != null) {
            builder.setSupplyKey(supplyKey.toProtobufKey());
        }
        if(feeScheduleKey != null) {
            builder.setFeeScheduleKey(feeScheduleKey.toProtobufKey());
        }
        builder.setFreezeDefault(freezeDefault);
        if(expirationTime != null) {
            builder.setExpiry(InstantConverter.toProtobuf(expirationTime));
        }
        if(autoRenewPeriod != null) {
            builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        }
        builder.setMemo(tokenMemo);
        builder.setTokenType(tokenType.code);
        builder.setSupplyType(tokenSupplyType.code);
        builder.setMaxSupply(maxSupply);

        for(var fee : customFees) {
            builder.addCustomFees(fee.toProtobuf());
        }

        return builder;
    }

    @Override
    void initFromTransactionBody(TransactionBody txBody) {
        var body = txBody.getTokenCreation();
        if (body.hasTreasury()) {
            treasuryAccountId = AccountId.fromProtobuf(body.getTreasury());
        }
        if(body.hasAutoRenewAccount()) {
            autoRenewAccountId = AccountId.fromProtobuf(body.getAutoRenewAccount());
        }
        tokenName = body.getName();
        tokenSymbol = body.getSymbol();
        decimals = body.getDecimals();
        initialSupply = body.getInitialSupply();
        if(body.hasAdminKey()) {
            adminKey = Key.fromProtobufKey(body.getAdminKey());
        }
        if(body.hasKycKey()) {
            kycKey = Key.fromProtobufKey(body.getKycKey());
        }
        if(body.hasFreezeKey()) {
            freezeKey = Key.fromProtobufKey(body.getFreezeKey());
        }
        if(body.hasWipeKey()) {
            wipeKey = Key.fromProtobufKey(body.getWipeKey());
        }
        if(body.hasSupplyKey()) {
            supplyKey = Key.fromProtobufKey(body.getSupplyKey());
        }
        if(body.hasFeeScheduleKey()) {
            feeScheduleKey = Key.fromProtobufKey(body.getFeeScheduleKey());
        }
        freezeDefault = body.getFreezeDefault();
        if(body.hasExpiry()) {
            expirationTime = InstantConverter.fromProtobuf(body.getExpiry());
        }
        if(body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }
        tokenMemo = body.getMemo();
        tokenType = TokenType.valueOf(body.getTokenType());
        tokenSupplyType = TokenSupplyType.valueOf(body.getSupplyType());
        maxSupply = body.getMaxSupply();

        for(var fee : body.getCustomFeesList()) {
            customFees.add(CustomFee.fromProtobuf(fee));
        }
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        for(var fee : customFees) {
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
