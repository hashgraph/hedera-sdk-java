package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.LinkedHashMap;

public class TokenUpdateTransaction extends Transaction<TokenUpdateTransaction> {
    private final TokenUpdateTransactionBody.Builder builder;

    public TokenUpdateTransaction() {
        builder = TokenUpdateTransactionBody.newBuilder();
    }

    TokenUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenUpdate().toBuilder();
    }

    TokenUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenUpdate().toBuilder();
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
        return Key.fromProtobufKey(builder.getAdminKey());
    }

    public TokenUpdateTransaction setAdminKey(Key key) {
        requireNotFrozen();
        builder.setAdminKey(key.toProtobufKey());
        return this;
    }

    public Key getKycKey() {
        return Key.fromProtobufKey(builder.getKycKey());
    }

    public TokenUpdateTransaction setKycKey(Key key) {
        requireNotFrozen();
        builder.setKycKey(key.toProtobufKey());
        return this;
    }

    public Key getFreezeKey() {
        return Key.fromProtobufKey(builder.getFreezeKey());
    }

    public TokenUpdateTransaction setFreezeKey(Key key) {
        requireNotFrozen();
        builder.setFreezeKey(key.toProtobufKey());
        return this;
    }

    public Key getWipeKey() {
        return Key.fromProtobufKey(builder.getWipeKey());
    }

    public TokenUpdateTransaction setWipeKey(Key key) {
        requireNotFrozen();
        builder.setWipeKey(key.toProtobufKey());
        return this;
    }

    public Key getSupplyKey() {
        return Key.fromProtobufKey(builder.getSupplyKey());
    }

    public TokenUpdateTransaction setSupplyKey(Key key) {
        requireNotFrozen();
        builder.setSupplyKey(key.toProtobufKey());
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

    public String getTokenMemo() {
        return builder.getMemo().getValue();
    }

    public TokenUpdateTransaction setTokenMemo(String memo) {
        requireNotFrozen();
        this.builder.setMemo(StringValue.of(memo));
        return this;
    }

    public TokenUpdateTransaction clearMemo() {
        requireNotFrozen();
        this.builder.clearMemo();
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

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenUpdate(builder);
    }
}
