package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenFreezeAccountTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

public class TokenFreezeTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenFreezeTransaction> {
    private final TokenFreezeAccountTransactionBody.Builder builder;

    public TokenFreezeTransaction() {
        builder = TokenFreezeAccountTransactionBody.newBuilder();
    }

    TokenFreezeTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenFreeze().toBuilder();
    }

    public TokenId getTokenId() {
        return TokenId.fromProtobuf(builder.getToken());
    }

    public TokenFreezeTransaction setTokenId(TokenId tokenId) {
        requireNotFrozen();
        builder.setToken(tokenId.toProtobuf());
        return this;
    }

    public AccountId getAccountId() {
        return AccountId.fromProtobuf(builder.getAccount());
    }

    public TokenFreezeTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        builder.setAccount(accountId.toProtobuf());
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getFreezeTokenAccountMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenFreeze(builder);
        return true;
    }
}
