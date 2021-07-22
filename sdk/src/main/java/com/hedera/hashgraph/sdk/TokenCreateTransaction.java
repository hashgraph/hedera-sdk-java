package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
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
    private final TokenCreateTransactionBody.Builder builder;

    List<CustomFee> customFees = new ArrayList<>();
    @Nullable
    AccountId treasuryAccountId = null;
    @Nullable
    AccountId autoRenewAccountId = null;

    public TokenCreateTransaction() {
        builder = TokenCreateTransactionBody.newBuilder();

        setAutoRenewPeriod(DEFAULT_AUTO_RENEW_PERIOD);
        setMaxTransactionFee(new Hbar(30));
    }

    TokenCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenCreation().toBuilder();

        if (builder.hasTreasury()) {
            treasuryAccountId = AccountId.fromProtobuf(builder.getTreasury());
        }

        for(var fee : builder.getCustomFeesList()) {
            customFees.add(CustomFee.fromProtobuf(fee));
        }
    }

    TokenCreateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenCreation().toBuilder();

        if (builder.hasTreasury()) {
            treasuryAccountId = AccountId.fromProtobuf(builder.getTreasury());
        }

        for(var fee : builder.getCustomFeesList()) {
            customFees.add(CustomFee.fromProtobuf(fee));
        }
    }

    @Nullable
    public String getTokenName() {
        return builder.getName();
    }

    public TokenCreateTransaction setTokenName(String name) {
        Objects.requireNonNull(name);
        requireNotFrozen();
        builder.setName(name);
        return this;
    }

    public String getTokenSymbol() {
        return builder.getSymbol();
    }

    public TokenCreateTransaction setTokenSymbol(String symbol) {
        Objects.requireNonNull(symbol);
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

    public Key getAdminKey() {
        return Key.fromProtobufKey(builder.getAdminKey());
    }

    public TokenCreateTransaction setAdminKey(Key key) {
        requireNotFrozen();
        builder.setAdminKey(key.toProtobufKey());
        return this;
    }

    public Key getKycKey() {
        return Key.fromProtobufKey(builder.getKycKey());
    }

    public TokenCreateTransaction setKycKey(Key key) {
        requireNotFrozen();
        builder.setKycKey(key.toProtobufKey());
        return this;
    }

    public Key getFreezeKey() {
        return Key.fromProtobufKey(builder.getFreezeKey());
    }

    public TokenCreateTransaction setFreezeKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        builder.setFreezeKey(key.toProtobufKey());
        return this;
    }

    public Key getWipeKey() {
        return Key.fromProtobufKey(builder.getWipeKey());
    }

    public TokenCreateTransaction setWipeKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        builder.setWipeKey(key.toProtobufKey());
        return this;
    }

    public Key getSupplyKey() {
        return Key.fromProtobufKey(builder.getSupplyKey());
    }

    public TokenCreateTransaction setSupplyKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        builder.setSupplyKey(key.toProtobufKey());
        return this;
    }

    public Key getFeeScheduleKey() {
        return Key.fromProtobufKey(builder.getFeeScheduleKey());
    }

    public TokenCreateTransaction setFeeScheduleKey(Key key) {
        requireNotFrozen();
        builder.setFeeScheduleKey(key.toProtobufKey());
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
        return InstantConverter.fromProtobuf(builder.getExpiry());
    }

    public TokenCreateTransaction setExpirationTime(Instant expirationTime) {
        Objects.requireNonNull(expirationTime);
        requireNotFrozen();
        builder.clearAutoRenewPeriod();
        builder.setExpiry(InstantConverter.toProtobuf(expirationTime));
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
        return DurationConverter.fromProtobuf(builder.getAutoRenewPeriod());
    }

    public TokenCreateTransaction setAutoRenewPeriod(Duration period) {
        Objects.requireNonNull(period);
        requireNotFrozen();
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(period));
        return this;
    }

    public String getTokenMemo() {
        return builder.getMemo();
    }

    public TokenCreateTransaction setTokenMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        this.builder.setMemo(memo);
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
        return TokenType.valueOf(builder.getTokenType());
    }

    public TokenCreateTransaction setTokenType(TokenType tokenType) {
        requireNotFrozen();
        if(tokenType == TokenType.NON_FUNGIBLE_UNIQUE) {
            /*
            Comments on initialSupply from protobuf:
            "Specifies the initial supply of tokens to be put in circulation.
            The initial supply is sent to the Treasury Account.
            The supply is in the lowest denomination possible.
            >>>In the case for NON_FUNGIBLE_UNIQUE Type the value must be 0<<<
            */
            setInitialSupply(0);
        }
        builder.setTokenType(tokenType.code);
        return this;
    }

    public TokenSupplyType getSupplyType() {
        return TokenSupplyType.valueOf(builder.getSupplyType());
    }

    public TokenCreateTransaction setSupplyType(TokenSupplyType supplyType) {
        requireNotFrozen();
        builder.setSupplyType(supplyType.code);
        return this;
    }

    public long getMaxSupply() {
        return builder.getMaxSupply();
    }

    public TokenCreateTransaction setMaxSupply(long maxSupply) {
        requireNotFrozen();
        builder.setMaxSupply(maxSupply);
        return this;
    }

    @Override
    public TokenCreateTransaction freezeWith(@Nullable Client client) {
        if (
            builder.hasAutoRenewPeriod() &&
                !builder.hasAutoRenewAccount() &&
                client != null &&
                client.getOperatorAccountId() != null
        ) {
            builder.setAutoRenewAccount(client.getOperatorAccountId().toProtobuf());
        }

        return super.freezeWith(client);
    }

    TokenCreateTransactionBody.Builder build() {
        if (treasuryAccountId != null) {
            builder.setTreasury(treasuryAccountId.toProtobuf());
        }

        if (autoRenewAccountId != null) {
            builder.setAutoRenewAccount(autoRenewAccountId.toProtobuf());
        }

        builder.clearCustomFees();
        for(var fee : customFees) {
            builder.addCustomFees(fee.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateNetworkOnIds(Client client) {
        for(var fee : customFees) {
            fee.validate(client);
        }

        if (treasuryAccountId != null) {
            treasuryAccountId.validate(client);
        }

        if (autoRenewAccountId != null) {
            autoRenewAccountId.validate(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getCreateTokenMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenCreation(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenCreation(build());
    }
}
