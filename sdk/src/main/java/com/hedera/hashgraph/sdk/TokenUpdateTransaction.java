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
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TokenUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

public class TokenUpdateTransaction extends Transaction<TokenUpdateTransaction> {
    @Nullable
    private TokenId tokenId = null;
    @Nullable
    private AccountId treasuryAccountId = null;
    @Nullable
    private AccountId autoRenewAccountId = null;
    private String tokenName = "";
    private String tokenSymbol = "";
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
    @Nullable
    private Key pauseKey = null;
    @Nullable
    private Instant expirationTime = null;
    @Nullable
    private Duration autoRenewPeriod = null;
    @Nullable
    private String tokenMemo = null;


    public TokenUpdateTransaction() {
    }

    TokenUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TokenUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenUpdateTransaction setTokenId(TokenId tokenId) {
        requireNotFrozen();
        Objects.requireNonNull(tokenId);
        this.tokenId = tokenId;
        return this;
    }

    @Nullable
    public String getTokenName() {
        return tokenName;
    }

    public TokenUpdateTransaction setTokenName(String name) {
        Objects.requireNonNull(name);
        requireNotFrozen();
        tokenName = name;
        return this;
    }

    public String getTokenSymbol() {
        return tokenSymbol;
    }

    public TokenUpdateTransaction setTokenSymbol(String symbol) {
        Objects.requireNonNull(symbol);
        requireNotFrozen();
        tokenSymbol = symbol;
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

    @Nullable
    public Key getAdminKey() {
        return adminKey;
    }

    public TokenUpdateTransaction setAdminKey(Key key) {
        requireNotFrozen();
        Objects.requireNonNull(key);
        adminKey = key;
        return this;
    }

    @Nullable
    public Key getKycKey() {
        return kycKey;
    }

    public TokenUpdateTransaction setKycKey(Key key) {
        requireNotFrozen();
        kycKey = key;
        return this;
    }

    @Nullable
    public Key getFreezeKey() {
        return freezeKey;
    }

    public TokenUpdateTransaction setFreezeKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        freezeKey = key;
        return this;
    }

    @Nullable
    public Key getWipeKey() {
        return wipeKey;
    }

    public TokenUpdateTransaction setWipeKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        wipeKey = key;
        return this;
    }

    @Nullable
    public Key getSupplyKey() {
        return supplyKey;
    }

    public TokenUpdateTransaction setSupplyKey(Key key) {
        Objects.requireNonNull(key);
        requireNotFrozen();
        supplyKey = key;
        return this;
    }

    @Nullable
    public Key getFeeScheduleKey() {
        return feeScheduleKey;
    }

    public TokenUpdateTransaction setFeeScheduleKey(Key key) {
        requireNotFrozen();
        Objects.requireNonNull(key);
        feeScheduleKey = key;
        return this;
    }

    @Nullable
    public Key getPauseKey() {
        return pauseKey;
    }

    public TokenUpdateTransaction setPauseKey(Key key) {
        requireNotFrozen();
        Objects.requireNonNull(key);
        pauseKey = key;
        return this;
    }

    @Nullable
    public Instant getExpirationTime() {
        return expirationTime;
    }

    public TokenUpdateTransaction setExpirationTime(Instant expirationTime) {
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

    public TokenUpdateTransaction setAutoRenewAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.autoRenewAccountId = accountId;
        return this;
    }

    @Nullable
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    public TokenUpdateTransaction setAutoRenewPeriod(Duration period) {
        Objects.requireNonNull(period);
        requireNotFrozen();
        autoRenewPeriod = period;
        return this;
    }

    @Nullable
    public String getTokenMemo() {
        return tokenMemo;
    }

    public TokenUpdateTransaction setTokenMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        tokenMemo = memo;
        return this;
    }

    public TokenUpdateTransaction clearMemo() {
        requireNotFrozen();
        tokenMemo = "";
        return this;
    }

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
