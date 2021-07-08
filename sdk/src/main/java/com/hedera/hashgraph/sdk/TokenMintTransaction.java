package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenMintTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import com.google.protobuf.ByteString;

import java.util.LinkedHashMap;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TokenMintTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenMintTransaction> {
    private final TokenMintTransactionBody.Builder builder;

    @Nullable
    TokenId tokenId = null;

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
        return builder.getAmount();
    }

    public TokenMintTransaction addMetadata(byte[] metadata) {
        requireNotFrozen();
        builder.addMetadata(ByteString.copyFrom(metadata));
        return this;
    }

    public TokenMintTransaction setMetadata(List<byte[]> metadatas) {
        requireNotFrozen();
        builder.clearMetadata();
        for(var metadata : Objects.requireNonNull(metadatas)) {
            builder.addMetadata(ByteString.copyFrom(metadata));
        }
        return this;
    }

    public List<byte[]> getMetadata() {
        var metadata = new ArrayList<byte[]>();
        for(var datum : builder.getMetadataList()) {
            metadata.add(datum.toByteArray());
        }
        return metadata;
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
    void validateNetworkOnIds(Client client) {
        if (tokenId != null) {
            tokenId.validate(client);
        }
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
