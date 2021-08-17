package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenMintTransactionBody;
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

public class TokenMintTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenMintTransaction> {
    @Nullable
    private TokenId tokenId = null;
    private List<byte[]> metadataList = new ArrayList<>();
    private long amount = 0;

    public TokenMintTransaction() {
    }

    TokenMintTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TokenMintTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenMintTransaction setTokenId(@Nullable TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    public long getAmount() {
        return amount;
    }

    public TokenMintTransaction setAmount(@Nonnegative long amount) {
        requireNotFrozen();
        this.amount = amount;
        return this;
    }

    public TokenMintTransaction addMetadata(byte[] metadata) {
        requireNotFrozen();
        Objects.requireNonNull(metadata);
        metadataList.add(metadata);
        return this;
    }

    public List<byte[]> getMetadata() {
        return new ArrayList<>(metadataList);
    }

    public TokenMintTransaction setMetadata(List<byte[]> metadataList) {
        requireNotFrozen();
        this.metadataList = new ArrayList<>(metadataList);
        return this;
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenMint();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }
        amount = body.getAmount();
        for (var metadata : body.getMetadataList()) {
            metadataList.add(metadata.toByteArray());
        }
    }

    TokenMintTransactionBody.Builder build() {
        var builder = TokenMintTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }
        builder.setAmount(amount);
        for (var metadata : metadataList) {
            builder.addMetadata(ByteString.copyFrom(metadata));
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
        return TokenServiceGrpc.getMintTokenMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenMint(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenMint(build());
    }
}
