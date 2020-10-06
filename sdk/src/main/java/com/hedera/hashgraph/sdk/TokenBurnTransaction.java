package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenBurnTransactionBody;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

public class TokenBurnTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenBurnTransaction> {
    private final TokenBurnTransactionBody.Builder builder;

    public TokenBurnTransaction() {
        builder = TokenBurnTransactionBody.newBuilder();
    }

    TokenBurnTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenBurn().toBuilder();
    }

    public TokenId getToken() {
        return TokenId.fromProtobuf(builder.getToken());
    }

    public TokenBurnTransaction setToken(TokenId tokenId) {
        requireNotFrozen();
        builder.setToken(tokenId.toProtobuf());
        return this;
    }

    public long getAmount() {
        return builder.getAmount();
    }

    public TokenBurnTransaction setAmount(long amount) {
        requireNotFrozen();
        builder.setAmount(amount);
        return this;
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getBurnTokenMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenBurn(builder);
        return true;
    }
}
