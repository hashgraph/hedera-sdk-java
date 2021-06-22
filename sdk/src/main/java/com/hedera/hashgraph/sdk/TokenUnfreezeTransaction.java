package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TokenUnfreezeTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenUnfreezeTransaction> {
    private final TokenUnfreezeAccountTransactionBody.Builder builder;

    TokenId tokenId;
    AccountId accountId;

    public TokenUnfreezeTransaction() {
        builder = TokenUnfreezeAccountTransactionBody.newBuilder();
    }

    TokenUnfreezeTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenUnfreeze().toBuilder();

        if (builder.hasToken()) {
            tokenId = TokenId.fromProtobuf(builder.getToken());
        }

        if (builder.hasAccount()) {
            accountId = AccountId.fromProtobuf(builder.getAccount());
        }
    }

    TokenUnfreezeTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenUnfreeze().toBuilder();

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

    public TokenUnfreezeTransaction setTokenId(TokenId tokenId) {
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

    public TokenUnfreezeTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    TokenUnfreezeAccountTransactionBody.Builder build() {
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
        bodyBuilder.setTokenUnfreeze(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenUnfreeze(build());
    }
}
