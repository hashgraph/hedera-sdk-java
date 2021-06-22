package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TokenMintTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenMintTransaction> {
    private final TokenMintTransactionBody.Builder builder;

    TokenId tokenId;

    public TokenMintTransaction() {
        builder = TokenMintTransactionBody.newBuilder();
    }

    TokenMintTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenMint().toBuilder();

        if (builder.hasToken()) {
            tokenId = TokenId.fromProtobuf(builder.getToken());
        }
    }

    TokenMintTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenMint().toBuilder();

        if (builder.hasToken()) {
            tokenId = TokenId.fromProtobuf(builder.getToken());
        }
    }

    public TokenId getTokenId() {
        if (tokenId == null) {
            return new TokenId(0);
        }

        return tokenId;
    }

    public TokenMintTransaction setTokenId(TokenId tokenId) {
        requireNotFrozen();
        this.tokenId = tokenId;
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

    TokenMintTransactionBody.Builder build() {
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateNetworkOnIds(@Nullable AccountId accountId) {
        EntityIdHelper.validateNetworkOnIds(this.tokenId, accountId);
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getMintTokenMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenMint(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenMint(build());
    }
}
