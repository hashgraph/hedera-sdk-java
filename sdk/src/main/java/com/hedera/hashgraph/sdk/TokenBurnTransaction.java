package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TokenBurnTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;

public class TokenBurnTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenBurnTransaction> {
    private final TokenBurnTransactionBody.Builder builder;

    TokenId tokenId;

    public TokenBurnTransaction() {
        builder = TokenBurnTransactionBody.newBuilder();
    }

    TokenBurnTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenBurn().toBuilder();

        if (builder.hasToken()) {
            tokenId = TokenId.fromProtobuf(builder.getToken());
        }
    }

    TokenBurnTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenBurn().toBuilder();

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

    public TokenBurnTransaction setTokenId(TokenId tokenId) {
        requireNotFrozen();
        this.tokenId = tokenId;
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

    TokenBurnTransactionBody.Builder build() {
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
        return TokenServiceGrpc.getBurnTokenMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenBurn(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenBurn(build());
    }
}
