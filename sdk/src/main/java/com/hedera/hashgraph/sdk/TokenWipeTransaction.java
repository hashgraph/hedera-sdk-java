package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenWipeAccountTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.List;

public class TokenWipeTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenWipeTransaction> {
    @Nullable
    private TokenId tokenId = null;
    @Nullable
    private AccountId accountId = null;
    private long amount = 0;
    private List<Long> serialList = new ArrayList<>();

    public TokenWipeTransaction() {
    }

    TokenWipeTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TokenWipeTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    public TokenWipeTransaction setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        requireNotFrozen();
        this.tokenId = tokenId;
        return this;
    }

    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    public TokenWipeTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    public long getAmount() {
        return amount;
    }

    public TokenWipeTransaction setAmount(long amount) {
        requireNotFrozen();
        this.amount = amount;
        return this;
    }

    public List<Long> getSerials() {
        return serialList;
    }

    public TokenWipeTransaction addSerial(long serial) {
        requireNotFrozen();
        serialList.add(serial);
        return this;
    }

    public TokenWipeTransaction setSerials(List<Long> serialList) {
        requireNotFrozen();
        Objects.requireNonNull(serialList);
        this.serialList = serialList;
        return this;
    }

    void initFromTransactionBody() {
        var body = txBody.getTokenWipe();
        if (body.hasToken()) {
            tokenId = TokenId.fromProtobuf(body.getToken());
        }

        if (body.hasAccount()) {
            accountId = AccountId.fromProtobuf(body.getAccount());
        }
        amount = body.getAmount();
        serialList = body.getSerialNumbersList();
    }

    TokenWipeAccountTransactionBody.Builder build() {
        var builder = TokenWipeAccountTransactionBody.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }

        if (accountId != null) {
            builder.setAccount(accountId.toProtobuf());
        }
        builder.setAmount(amount);
        for(var serial : serialList) {
            builder.addSerialNumbers(serial);
        }

        return  builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }

        if (accountId != null) {
            accountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getWipeTokenAccountMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenWipe(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenWipe(build());
    }
}

