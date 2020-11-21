package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

import java.time.Duration;
import java.time.Instant;

public class TokenUpdateTransaction extends Transaction<TokenUpdateTransaction> {
    private final TokenUpdateTransactionBody.Builder builder;

    public TokenUpdateTransaction() {
        builder = TokenUpdateTransactionBody.newBuilder();
    }

    TokenUpdateTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenUpdate().toBuilder();
    }

    public TokenId getTokenId() {
        return  TokenId.fromProtobuf(builder.getToken());
    }

    public TokenUpdateTransaction setTokenId(TokenId tokenId) {
        requireNotFrozen();
        builder.setToken(tokenId.toProtobuf());
        return this;
    }

    public String getTokenName() {
        return builder.getName();
    }

    public TokenUpdateTransaction setTokenName(String name) {
        requireNotFrozen();
        builder.setName(name);
        return this;
    }

    public String getTokenSymbol() {
        return builder.getSymbol();
    }

    public TokenUpdateTransaction setTokenSymbol(String symbol) {
        requireNotFrozen();
        builder.setSymbol(symbol);
        return this;
    }

    public AccountId getTreasuryAccountId() {
        return AccountId.fromProtobuf(builder.getTreasury());
    }

    public TokenUpdateTransaction setTreasuryAccountId(AccountId accountId) {
        requireNotFrozen();
        builder.setTreasury(accountId.toProtobuf());
        return this;
    }

    public Key getAdminKey() {
        return Key.fromProtobuf(builder.getAdminKey());
    }

    public TokenUpdateTransaction setAdminKey(Key key) {
        requireNotFrozen();
        builder.setAdminKey(key.toKeyProtobuf());
        return this;
    }

    public Key getKycKey() {
        return Key.fromProtobuf(builder.getKycKey());
    }

    public TokenUpdateTransaction setKycKey(Key key) {
        requireNotFrozen();
        builder.setKycKey(key.toKeyProtobuf());
        return this;
    }

    public Key getFreezeKey() {
        return Key.fromProtobuf(builder.getFreezeKey());
    }

    public TokenUpdateTransaction setFreezeKey(Key key) {
        requireNotFrozen();
        builder.setFreezeKey(key.toKeyProtobuf());
        return this;
    }

    public Key getWipeKey() {
        return Key.fromProtobuf(builder.getWipeKey());
    }

    public TokenUpdateTransaction setWipeKey(Key key) {
        requireNotFrozen();
        builder.setWipeKey(key.toKeyProtobuf());
        return this;
    }

    public Key getSupplyKey() {
        return Key.fromProtobuf(builder.getSupplyKey());
    }

    public TokenUpdateTransaction setSupplyKey(Key key) {
        requireNotFrozen();
        builder.setSupplyKey(key.toKeyProtobuf());
        return this;
    }

    public Instant getExpirationTime() {
        return InstantConverter.fromProtobuf(builder.getExpiry());
    }

    public TokenUpdateTransaction setExpirationTime(Instant expirationTime) {
        requireNotFrozen();
        builder.setExpiry(InstantConverter.toProtobuf(expirationTime));
        return this;
    }

    public AccountId getAutoRenewAccountId() {
        return AccountId.fromProtobuf(builder.getAutoRenewAccount());
    }

    public TokenUpdateTransaction setAutoRenewAccountId(AccountId accountId) {
        requireNotFrozen();
        builder.setAutoRenewAccount(accountId.toProtobuf());
        return this;
    }

    public Duration getAutoRenewPeriod() {
        return DurationConverter.fromProtobuf(builder.getAutoRenewPeriod());
    }

    public TokenUpdateTransaction setAutoRenewPeriod(Duration period) {
        requireNotFrozen();
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(period));
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getUpdateTokenMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenUpdate(builder);
        return true;
    }
}
