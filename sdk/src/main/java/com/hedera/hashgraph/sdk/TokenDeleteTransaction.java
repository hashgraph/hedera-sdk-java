package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class TokenDeleteTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenDeleteTransaction> {
    private final TokenDeleteTransactionBody.Builder builder;

    public TokenDeleteTransaction() {
        builder = TokenDeleteTransactionBody.newBuilder();
    }

    TokenDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenDeletion().toBuilder();
    }

    TokenDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) throws InvalidProtocolBufferException {
        super(txBody);

        builder = bodyBuilder.getTokenDeletion().toBuilder();
    }

    public TokenId getTokenId() {
        return TokenId.fromProtobuf(builder.getToken());
    }

    public TokenDeleteTransaction setTokenId(TokenId tokenId) {
        requireNotFrozen();
        builder.setToken(tokenId.toProtobuf());
        return this;
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getDeleteTokenMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenDeletion(builder);
        return true;
    }
}
