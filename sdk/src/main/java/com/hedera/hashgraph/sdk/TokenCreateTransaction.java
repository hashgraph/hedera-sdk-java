package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

public class TokenCreateTransaction extends Transaction<TokenCreateTransaction> {
    private final TokenCreateTransactionBody.Builder builder;

    public TokenCreateTransaction() {
        builder = TokenCreateTransactionBody.newBuilder();

        setAutoRenewPeriod(DEFAULT_AUTO_RENEW_PERIOD);
        setExpirationTime(Instant.now().plus(Duration.ofDays(90)));
    }

    TokenCreateTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenCreation().toBuilder();
    }

    public String getTokenName() {
        return builder.getName();
    }

    public TokenCreateTransaction setTokenName(String name) {
        requireNotFrozen();
        builder.setName(name);
        return this;
    }

    public String getTokenSymbol() {
        return builder.getSymbol();
    }

    public TokenCreateTransaction setTokenSymbol(String symbol) {
        requireNotFrozen();
        builder.setSymbol(symbol);
        return this;
    }

    public int getDecimals() {
        return builder.getDecimals();
    }

    public TokenCreateTransaction setDecimals(int decimals) {
        requireNotFrozen();
        builder.setDecimals(decimals);
        return this;
    }

    public long getInitialSupply() {
        return builder.getInitialSupply();
    }

    public TokenCreateTransaction setInitialSupply(long initialSupply) {
        requireNotFrozen();
        builder.setInitialSupply(initialSupply);
        return this;
    }

    public AccountId getTreasuryAccountId() {
        return AccountId.fromProtobuf(builder.getTreasury());
    }

    public TokenCreateTransaction setTreasuryAccountId(AccountId accountId) {
        requireNotFrozen();
        builder.setTreasury(accountId.toProtobuf());
        return this;
    }

    public Key getAdminKey() {
        return Key.fromProtobuf(builder.getAdminKey());
    }

    public TokenCreateTransaction setAdminKey(Key key) {
        requireNotFrozen();
        builder.setAdminKey(key.toKeyProtobuf());
        return this;
    }

    public Key getKycKey() {
        return Key.fromProtobuf(builder.getKycKey());
    }

    public TokenCreateTransaction setKycKey(Key key) {
        requireNotFrozen();
        builder.setKycKey(key.toKeyProtobuf());
        return this;
    }

    public Key getFreezeKey() {
        return Key.fromProtobuf(builder.getFreezeKey());
    }

    public TokenCreateTransaction setFreezeKey(Key key) {
        requireNotFrozen();
        builder.setFreezeKey(key.toKeyProtobuf());
        return this;
    }

    public Key getWipeKey() {
        return Key.fromProtobuf(builder.getWipeKey());
    }

    public TokenCreateTransaction setWipeKey(Key key) {
        requireNotFrozen();
        builder.setWipeKey(key.toKeyProtobuf());
        return this;
    }

    public Key getSupplyKey() {
        return Key.fromProtobuf(builder.getSupplyKey());
    }

    public TokenCreateTransaction setSupplyKey(Key key) {
        requireNotFrozen();
        builder.setSupplyKey(key.toKeyProtobuf());
        return this;
    }

    public boolean getFreezeDefault() {
        return builder.getFreezeDefault();
    }

    public TokenCreateTransaction setFreezeDefault(boolean freezeDefault) {
        requireNotFrozen();
        builder.setFreezeDefault(freezeDefault);
        return this;
    }

    public Instant getExpirationTime() {
        return Instant.ofEpochSecond(builder.getExpiry());
    }

    public TokenCreateTransaction setExpirationTime(Instant expirationTime) {
        requireNotFrozen();
        builder.setExpiry(expirationTime.getEpochSecond());
        return this;
    }

    public AccountId getAutoRenewAccountId() {
        return AccountId.fromProtobuf(builder.getAutoRenewAccount());
    }

    public TokenCreateTransaction setAutoRenewAccountId(AccountId accountId) {
        requireNotFrozen();
        builder.setAutoRenewAccount(accountId.toProtobuf());
        return this;
    }

    public Duration getAutoRenewPeriod() {
        return Duration.ofSeconds(builder.getAutoRenewPeriod());
    }

    public TokenCreateTransaction setAutoRenewPeriod(Duration period) {
        requireNotFrozen();
        builder.setAutoRenewPeriod(period.getSeconds());
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getCreateTokenMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenCreation(builder);
        return true;
    }
}
