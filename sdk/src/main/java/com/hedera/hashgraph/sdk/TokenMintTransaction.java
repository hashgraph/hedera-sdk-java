package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenMintTransactionBody;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

public class TokenMintTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenMintTransaction> {
    private final TokenMintTransactionBody.Builder builder;

    public TokenMintTransaction() {
        builder = TokenMintTransactionBody.newBuilder();
    }

    TokenMintTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenMint().toBuilder();
    }

    public TokenId getToken() {
        return TokenId.fromProtobuf(builder.getToken());
    }

    public TokenMintTransaction setToken(TokenId tokenId) {
        requireNotFrozen();
        builder.setToken(tokenId.toProtobuf());
        return this;
    }

    public long getAmount() {
        return builder.getAmount();
    }

    public TokenMintTransaction setAmount(long amount) {
        requireNotFrozen();
        builder.setAmount(amount);
        return this;
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getMintTokenMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenMint(builder);
        return true;
    }
}
