package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenUnfreezeAccountTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.LinkedHashMap;
import javax.annotation.Nullable;
import java.util.Objects;

public class TokenUnfreezeTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenUnfreezeTransaction> {
    @Nullable
    private TokenId tokenId = null;
    @Nullable
    private AccountId accountId = null;

    public TokenUnfreezeTransaction() {
    }

    TokenUnfreezeTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
    }

    TokenUnfreezeTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
    }

    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenUnfreezeTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    public TokenUnfreezeTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    @Override
    void initFromTransactionBody(TransactionBody txBody) {
        var body = txBody.getTokenUnfreeze();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }

        if (body.hasAccount()) {
            accountId = AccountId.fromProtobuf(body.getAccount());
        }
    }

    TokenUnfreezeAccountTransactionBody.Builder build() {
        var builder = TokenUnfreezeAccountTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }

        if (accountId != null) {
            builder.setAccount(accountId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }

        if (accountId != null) {
            accountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getFreezeTokenAccountMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenUnfreeze(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenUnfreeze(build());
    }
}
