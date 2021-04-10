package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class TokenMintTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenMintTransaction> {
    private final TokenMintTransactionBody.Builder builder;

    public TokenMintTransaction() {
        builder = TokenMintTransactionBody.newBuilder();
    }

    TokenMintTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenMint().toBuilder();
    }

    TokenMintTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenMint().toBuilder();
    }

    public TokenId getTokenId() {
        return TokenId.fromProtobuf(builder.getToken());
    }

    public TokenMintTransaction setTokenId(TokenId tokenId) {
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

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenMint(builder);
    }
}
