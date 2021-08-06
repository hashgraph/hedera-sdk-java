package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.LinkedHashMap;
import javax.annotation.Nullable;
import java.util.Objects;

public class TokenDeleteTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenDeleteTransaction> {
    @Nullable
    private TokenId tokenId = null;

    public TokenDeleteTransaction() {
    }

    TokenDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
    }

    TokenDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
    }

    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenDeleteTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    @Override
    void initFromTransactionBody(TransactionBody txBody) {
        var body = txBody.getTokenDeletion();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }
    }

    TokenDeleteTransactionBody.Builder build() {
        var builder = TokenDeleteTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getDeleteTokenMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenDeletion(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenDeletion(build());
    }
}
