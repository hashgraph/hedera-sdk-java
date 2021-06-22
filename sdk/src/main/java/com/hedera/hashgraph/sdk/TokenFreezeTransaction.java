package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TokenFreezeTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenFreezeTransaction> {
    private final TokenFreezeAccountTransactionBody.Builder builder;

    TokenId tokenId;
    AccountId accountId;

    public TokenFreezeTransaction() {
        builder = TokenFreezeAccountTransactionBody.newBuilder();
    }

    TokenFreezeTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenFreeze().toBuilder();

        if (builder.hasToken()) {
            tokenId = TokenId.fromProtobuf(builder.getToken());
        }

        if (builder.hasAccount()) {
            accountId = AccountId.fromProtobuf(builder.getAccount());
        }
    }

    TokenFreezeTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenFreeze().toBuilder();

        if (builder.hasToken()) {
            tokenId = TokenId.fromProtobuf(builder.getToken());
        }

        if (builder.hasAccount()) {
            accountId = AccountId.fromProtobuf(builder.getAccount());
        }
    }

    public TokenId getTokenId() {
        if (tokenId == null) {
            return new TokenId(0);
        }

        return tokenId;
    }

    public TokenFreezeTransaction setTokenId(TokenId tokenId) {
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    public AccountId getAccountId() {
        if (accountId == null) {
            return new AccountId(0);
        }

        return accountId;
    }

    public TokenFreezeTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    TokenFreezeAccountTransactionBody.Builder build() {
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }

        if (accountId != null) {
            builder.setAccount(accountId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateNetworkOnIds(@Nullable AccountId accountId) {
        EntityIdHelper.validateNetworkOnIds(this.tokenId, accountId);
        EntityIdHelper.validateNetworkOnIds(this.accountId, accountId);
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getFreezeTokenAccountMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenFreeze(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenFreeze(build());
    }
}
