package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.TokenUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.time.Duration;
import java.time.Instant;

import java.util.LinkedHashMap;
import java.util.Objects;
import javax.annotation.Nullable;

public class TokenUpdateTransaction extends Transaction<TokenUpdateTransaction> {
    private final TokenUpdateTransactionBody.Builder builder;

    @Nullable
    TokenId tokenId = null;
    @Nullable
    AccountId treasuryAccountId = null;
    @Nullable
    AccountId autoRenewAccountId = null;


    public TokenUpdateTransaction() {
        builder = TokenUpdateTransactionBody.newBuilder();
    }

    TokenUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenUpdate().toBuilder();

        if (builder.hasToken()) {
            tokenId = TokenId.fromProtobuf(builder.getToken());
        }

        if (builder.hasTreasury()) {
            treasuryAccountId = AccountId.fromProtobuf(builder.getTreasury());
        }

        if(builder.hasAutoRenewAccount()) {
            autoRenewAccountId = AccountId.fromProtobuf(builder.getAutoRenewAccount());
        }
    }

    TokenUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenUpdate().toBuilder();

        if (builder.hasToken()) {
            tokenId = TokenId.fromProtobuf(builder.getToken());
        }

        if (builder.hasTreasury()) {
            treasuryAccountId = AccountId.fromProtobuf(builder.getTreasury());
        }

        if(builder.hasAutoRenewAccount()) {
            autoRenewAccountId = AccountId.fromProtobuf(builder.getAutoRenewAccount());
        }
    }

    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenUpdateTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    public String getTokenName() {
        return builder.getName();
    }

    public TokenUpdateTransaction setTokenName(String name) {
        Objects.requireNonNull(name);
        requireNotFrozen();
        builder.setName(name);
        return this;
    }

    public String getTokenSymbol() {
        return builder.getSymbol();
    }

    public TokenUpdateTransaction setTokenSymbol(String symbol) {
        Objects.requireNonNull(symbol);
        requireNotFrozen();
        builder.setSymbol(symbol);
        return this;
    }

    @Nullable
    public AccountId getTreasuryAccountId() {
        return treasuryAccountId;
    }

    public TokenUpdateTransaction setTreasuryAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.treasuryAccountId = accountId;
        return this;
    }

    public Key getAdminKey() {
        return Key.fromProtobufKey(builder.getAdminKey());
    }

    public TokenUpdateTransaction setAdminKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        builder.setAdminKey(key.toProtobufKey());
        return this;
    }

    public Key getKycKey() {
        return Key.fromProtobufKey(builder.getKycKey());
    }

    public TokenUpdateTransaction setKycKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        builder.setKycKey(key.toProtobufKey());
        return this;
    }

    public Key getFreezeKey() {
        return Key.fromProtobufKey(builder.getFreezeKey());
    }

    public TokenUpdateTransaction setFreezeKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        builder.setFreezeKey(key.toProtobufKey());
        return this;
    }

    public Key getWipeKey() {
        return Key.fromProtobufKey(builder.getWipeKey());
    }

    public TokenUpdateTransaction setWipeKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        builder.setWipeKey(key.toProtobufKey());
        return this;
    }

    public Key getSupplyKey() {
        return Key.fromProtobufKey(builder.getSupplyKey());
    }

    public TokenUpdateTransaction setSupplyKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        builder.setSupplyKey(key.toProtobufKey());
        return this;
    }

    public Key getFeeScheduleKey() {
        return Key.fromProtobufKey(builder.getFeeScheduleKey());
    }

    public TokenUpdateTransaction setFeeScheduleKey(Key key) {
        requireNotFrozen();
        builder.setFeeScheduleKey(key.toProtobufKey());
        return this;
    }

    public Instant getExpirationTime() {
        return InstantConverter.fromProtobuf(builder.getExpiry());
    }

    public TokenUpdateTransaction setExpirationTime(Instant expirationTime) {
        Objects.requireNonNull(expirationTime);
        requireNotFrozen();
        builder.setExpiry(InstantConverter.toProtobuf(expirationTime));
        return this;
    }

    @Nullable
    public AccountId getAutoRenewAccountId() {
        return autoRenewAccountId;
    }

    public TokenUpdateTransaction setAutoRenewAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.autoRenewAccountId = accountId;
        return this;
    }

    public Duration getAutoRenewPeriod() {
        return DurationConverter.fromProtobuf(builder.getAutoRenewPeriod());
    }

    public TokenUpdateTransaction setAutoRenewPeriod(Duration period) {
        Objects.requireNonNull(period);
        requireNotFrozen();
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(period));
        return this;
    }

    public String getTokenMemo() {
        return builder.getMemo().getValue();
    }

    public TokenUpdateTransaction setTokenMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        this.builder.setMemo(StringValue.of(memo));
        return this;
    }

    public TokenUpdateTransaction clearMemo() {
        requireNotFrozen();
        this.builder.clearMemo();
        return this;
    }

    TokenUpdateTransactionBody.Builder build() {
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }

        if (treasuryAccountId != null) {
            builder.setTreasury(treasuryAccountId.toProtobuf());
        }

        if (autoRenewAccountId != null) {
            builder.setAutoRenewAccount(autoRenewAccountId.toProtobuf());
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
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenUpdate(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenUpdate(build());
    }
}
