package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

public class TokenUpdateTransaction extends Transaction<TokenUpdateTransaction> {
    private final TokenUpdateTransactionBody.Builder builder;

    public TokenUpdateTransaction() {
        builder = TokenUpdateTransactionBody.newBuilder();

        setAutoRenewPeriod(DEFAULT_AUTO_RENEW_PERIOD.toMillis() / 1000);
    }

    TokenUpdateTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenUpdate().toBuilder();
    }

    public TokenId getToken() {
        return  TokenId.fromProtobuf(builder.getToken());
    }

    public TokenUpdateTransaction setToken(TokenId tokenId) {
        requireNotFrozen();
        builder.setToken(tokenId.toProtobuf());
        return this;
    }

    public String getName() {
        return builder.getName();
    }

    public TokenUpdateTransaction setName(String name) {
        requireNotFrozen();
        builder.setName(name);
        return this;
    }

    public String getSymbol() {
        return builder.getSymbol();
    }

    public TokenUpdateTransaction setSymbol(String symbol) {
        requireNotFrozen();
        builder.setSymbol(symbol);
        return this;
    }

    public AccountId getTreasury() {
        return AccountId.fromProtobuf(builder.getTreasury());
    }

    public TokenUpdateTransaction setTreasury(AccountId accountId) {
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

    public long getExpiry() {
        return builder.getExpiry();
    }

    public TokenUpdateTransaction setExpiry(long expiry) {
        requireNotFrozen();
        builder.setExpiry(expiry);
        return this;
    }

    public AccountId getAutoRenewAccount() {
        return AccountId.fromProtobuf(builder.getAutoRenewAccount());
    }

    public TokenUpdateTransaction setAutoRenewAccount(AccountId accountId) {
        requireNotFrozen();
        builder.setAutoRenewAccount(accountId.toProtobuf());
        return this;
    }

    public long getAutoRenewPeriod() {
        return builder.getAutoRenewPeriod();
    }

    public TokenUpdateTransaction setAutoRenewPeriod(long period) {
        requireNotFrozen();
        builder.setAutoRenewPeriod(period);
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
