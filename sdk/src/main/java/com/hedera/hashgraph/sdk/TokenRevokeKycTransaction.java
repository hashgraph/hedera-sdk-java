package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class TokenRevokeKycTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenRevokeKycTransaction> {
    private final TokenRevokeKycTransactionBody.Builder builder;

    public TokenRevokeKycTransaction() {
        builder = TokenRevokeKycTransactionBody.newBuilder();
    }

    TokenRevokeKycTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenRevokeKyc().toBuilder();
    }

    TokenRevokeKycTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenRevokeKyc().toBuilder();
    }

    public TokenId getTokenId() {
        return TokenId.fromProtobuf(builder.getToken());
    }

    public TokenRevokeKycTransaction setTokenId(TokenId tokenId) {
        requireNotFrozen();
        builder.setToken(tokenId.toProtobuf());
        return this;
    }

    public AccountId getAccountId() {
        return AccountId.fromProtobuf(builder.getAccount());
    }

    public TokenRevokeKycTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        builder.setAccount(accountId.toProtobuf());
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getRevokeKycFromTokenAccountMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenRevokeKyc(builder);
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenRevokeKyc(builder);
    }
}
