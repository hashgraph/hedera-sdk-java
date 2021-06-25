package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class TokenAssociateTransaction extends Transaction<TokenAssociateTransaction> {
    private final TokenAssociateTransactionBody.Builder builder;

    AccountId accountId;
    List<TokenId> tokenIds = new ArrayList<>();

    public TokenAssociateTransaction() {
        builder = TokenAssociateTransactionBody.newBuilder();

        setMaxTransactionFee(new Hbar(5));
    }

    TokenAssociateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenAssociate().toBuilder();

        if (builder.hasAccount()) {
            accountId = AccountId.fromProtobuf(builder.getAccount());
        }

        for (var token : builder.getTokensList()) {
            tokenIds.add(TokenId.fromProtobuf(token));
        }
    }

    TokenAssociateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenAssociate().toBuilder();

        if (builder.hasAccount()) {
            accountId = AccountId.fromProtobuf(builder.getAccount());
        }

        for (var token : builder.getTokensList()) {
            tokenIds.add(TokenId.fromProtobuf(token));
        }
    }

    public AccountId getAccountId() {
        if (accountId != null) {
            return new AccountId(0);
        }

        return accountId;
    }

    public TokenAssociateTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    public List<TokenId> getTokenIds() {
        return tokenIds;
    }

    public TokenAssociateTransaction setTokenIds(List<TokenId> tokens) {
        requireNotFrozen();
        this.tokenIds = tokens;
        return this;
    }

    TokenAssociateTransactionBody.Builder build() {
        if (accountId != null) {
            builder.setAccount(accountId.toProtobuf());
        }

        for (var token : tokenIds) {
            if (token != null) {
                builder.addTokens(token.toProtobuf());
            }
        }

        return builder;
    }

    @Override
    void validateNetworkOnIds(Client client) {
        if (accountId != null) {
            accountId.validate(client);
        }

        for (var token : tokenIds) {
            if (token != null) {
                token.validate(client);
            }
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getAssociateTokensMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenAssociate(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenAssociate(build());
    }
}
