package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenRevokeKycTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

public class TokenRevokeKycTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenRevokeKycTransaction> {
    @Nullable
    private TokenId tokenId = null;
    @Nullable
    private AccountId accountId = null;

    public TokenRevokeKycTransaction() {
    }

    TokenRevokeKycTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TokenRevokeKycTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenRevokeKycTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    public TokenRevokeKycTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenRevokeKyc();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }

        if (body.hasAccount()) {
            accountId = AccountId.fromProtobuf(body.getAccount());
        }
    }

    TokenRevokeKycTransactionBody.Builder build() {
        var builder = TokenRevokeKycTransactionBody.newBuilder();
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
        bodyBuilder.setTokenRevokeKyc(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenRevokeKyc(build());
    }
}
