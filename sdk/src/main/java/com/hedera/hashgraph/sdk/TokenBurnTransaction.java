package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenBurnTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class TokenBurnTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenBurnTransaction> {
    @Nullable
    private TokenId tokenId = null;
    private long amount = 0;
    private List<Long> serials = new ArrayList<>();

    public TokenBurnTransaction() {
    }

    TokenBurnTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TokenBurnTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenBurnTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    public long getAmount() {
        return amount;
    }

    public TokenBurnTransaction setAmount(@Nonnegative long amount) {
        requireNotFrozen();
        this.amount = amount;
        return this;
    }

    public List<Long> getSerials() {
        return new ArrayList<>(serials);
    }

    public TokenBurnTransaction setSerials(List<Long> serials) {
        requireNotFrozen();
        Objects.requireNonNull(serials);
        this.serials = new ArrayList<>(serials);
        return this;
    }

    public TokenBurnTransaction addSerial(@Nonnegative long serial) {
        requireNotFrozen();
        serials.add(serial);
        return this;
    }

    TokenBurnTransactionBody.Builder build() {
        var builder = TokenBurnTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }
        builder.setAmount(amount);

        for (var serial : serials) {
            builder.addSerialNumbers(serial);
        }

        return builder;
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenBurn();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }
        amount = body.getAmount();
        serials = body.getSerialNumbersList();
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getBurnTokenMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenBurn(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenBurn(build());
    }
}
