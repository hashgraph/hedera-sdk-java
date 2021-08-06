package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TokenBurnTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.List;

public class TokenBurnTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenBurnTransaction> {
    @Nullable
    private TokenId tokenId = null;
    private long amount = 0;
    private List<Long> serials = new ArrayList<>();

    public TokenBurnTransaction() {
    }

    TokenBurnTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
    }

    TokenBurnTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
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

    public TokenBurnTransaction setAmount(long amount) {
        requireNotFrozen();
        this.amount = amount;
        return this;
    }

    public List<Long> getSerials() {
        return serials;
    }

    public TokenBurnTransaction addSerial(long serial) {
        requireNotFrozen();
        serials.add(serial);
        return this;
    }

    public TokenBurnTransaction setSerials(List<Long> serials) {
        requireNotFrozen();
        Objects.requireNonNull(serials);
        this.serials = serials;
        return this;
    }

    TokenBurnTransactionBody.Builder build() {
        var builder = TokenBurnTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }
        builder.setAmount(amount);

        for(var serial : serials) {
            builder.addSerialNumbers(serial);
        }

        return builder;
    }

    @Override
    void initFromTransactionBody(TransactionBody txBody) {
        var body = txBody.getTokenBurn();
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
