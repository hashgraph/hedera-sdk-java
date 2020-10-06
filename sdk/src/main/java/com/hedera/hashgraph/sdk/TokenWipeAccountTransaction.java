package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenWipeAccountTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

public class TokenWipeAccountTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenWipeAccountTransaction> {
    private final TokenWipeAccountTransactionBody.Builder builder;

    public TokenWipeAccountTransaction() {
        builder = TokenWipeAccountTransactionBody.newBuilder();
    }

    TokenWipeAccountTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenWipe().toBuilder();
    }

    public TokenId getToken() {
        return TokenId.fromProtobuf(builder.getToken());
    }

    public TokenWipeAccountTransaction setToken(TokenId tokenId) {
        requireNotFrozen();
        builder.setToken(tokenId.toProtobuf());
        return this;
    }

    public AccountId getAccount() {
        return AccountId.fromProtobuf(builder.getAccount());
    }

    public TokenWipeAccountTransaction setAccount(AccountId accountId) {
        requireNotFrozen();
        builder.setAccount(accountId.toProtobuf());
        return this;
    }

    public long getAmount() {
        return builder.getAmount();
    }

    public TokenWipeAccountTransaction setAmount(long amount) {
        requireNotFrozen();
        builder.setAmount(amount);
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getWipeTokenAccountMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenWipe(builder);
        return true;
    }
}

