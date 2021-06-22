package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class TokenDeleteTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenDeleteTransaction> {
    private final TokenDeleteTransactionBody.Builder builder;

    TokenId tokenId;

    public TokenDeleteTransaction() {
        builder = TokenDeleteTransactionBody.newBuilder();
    }

    TokenDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenDeletion().toBuilder();

        if (builder.hasToken()) {
            tokenId = TokenId.fromProtobuf(builder.getToken());
        }
    }

    TokenDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenDeletion().toBuilder();

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

    public TokenDeleteTransaction setTokenId(TokenId tokenId) {
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    TokenDeleteTransactionBody.Builder build() {
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
        return TokenServiceGrpc.getDeleteTokenMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenDeletion(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenDeletion(build());
    }
}
