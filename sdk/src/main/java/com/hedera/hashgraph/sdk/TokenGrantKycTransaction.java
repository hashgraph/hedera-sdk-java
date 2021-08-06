package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenGrantKycTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.LinkedHashMap;
import javax.annotation.Nullable;
import java.util.Objects;

public class TokenGrantKycTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenGrantKycTransaction> {
    @Nullable
    private TokenId tokenId = null;
    @Nullable
    private AccountId accountId = null;

    public TokenGrantKycTransaction() {
    }

    TokenGrantKycTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
    }

    TokenGrantKycTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
    }

    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenGrantKycTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    public TokenGrantKycTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    @Override
    void initFromTransactionBody(TransactionBody txBody) {
        var body = txBody.getTokenGrantKyc();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }

        if (body.hasAccount()) {
            accountId = AccountId.fromProtobuf(body.getAccount());
        }
    }

    TokenGrantKycTransactionBody.Builder build() {
        var builder = TokenGrantKycTransactionBody.newBuilder();
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
        bodyBuilder.setTokenGrantKyc(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenGrantKyc(build());
    }
}
