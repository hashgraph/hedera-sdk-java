package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenRevokeKycTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hederahashgraph.service.proto.java.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

public class TokenRevokeKycTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenRevokeKycTransaction> {
    private final TokenRevokeKycTransactionBody.Builder builder;

    public TokenRevokeKycTransaction() {
        builder = TokenRevokeKycTransactionBody.newBuilder();
    }

    TokenRevokeKycTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenRevokeKyc().toBuilder();
    }

    public TokenId getToken() {
        return TokenId.fromProtobuf(builder.getToken());
    }

    public TokenRevokeKycTransaction setToken(TokenId tokenId) {
        requireNotFrozen();
        builder.setToken(tokenId.toProtobuf());
        return this;
    }

    public AccountId getAccount() {
        return AccountId.fromProtobuf(builder.getAccount());
    }

    public TokenRevokeKycTransaction setAccount(AccountId accountId) {
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
}
