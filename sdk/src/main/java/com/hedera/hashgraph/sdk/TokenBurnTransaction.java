package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class TokenBurnTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenBurnTransaction> {
    private final TokenBurnTransactionBody.Builder builder;

    public TokenBurnTransaction() {
        builder = TokenBurnTransactionBody.newBuilder();
    }

    TokenBurnTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenBurn().toBuilder();
    }

    TokenBurnTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenBurn().toBuilder();
    }

    public TokenId getTokenId() {
        return TokenId.fromProtobuf(builder.getToken());
    }

    public TokenBurnTransaction setTokenId(TokenId tokenId) {
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

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenBurn(builder);
    }
}
