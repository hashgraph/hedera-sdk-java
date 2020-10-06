package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenUnfreezeAccountTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

public class TokenUnfreezeAccountTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenUnfreezeAccountTransaction> {
    private final TokenUnfreezeAccountTransactionBody.Builder builder;

    public TokenUnfreezeAccountTransaction() {
        builder = TokenUnfreezeAccountTransactionBody.newBuilder();
    }

    TokenUnfreezeAccountTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenUnfreeze().toBuilder();
    }

    public TokenId getToken() {
        return TokenId.fromProtobuf(builder.getToken());
    }

    public TokenUnfreezeAccountTransaction setToken(TokenId tokenId) {
        requireNotFrozen();
        builder.setToken(tokenId.toProtobuf());
        return this;
    }

    public AccountId getAccount() {
        return AccountId.fromProtobuf(builder.getAccount());
    }

    public TokenUnfreezeAccountTransaction setAccount(AccountId accountId) {
        requireNotFrozen();
        builder.setAccount(accountId.toProtobuf());
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getUnfreezeTokenAccountMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenUnfreeze(builder);
        return true;
    }
}
