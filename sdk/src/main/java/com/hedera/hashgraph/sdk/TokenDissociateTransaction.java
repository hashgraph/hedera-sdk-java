package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class TokenDissociateTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenDissociateTransaction> {
    private final TokenDissociateTransactionBody.Builder builder;

    AccountId accountId;
    List<TokenId> tokenIds = new ArrayList<>();

    public TokenDissociateTransaction() {
        builder = TokenDissociateTransactionBody.newBuilder();

        setMaxTransactionFee(new Hbar(5));
    }

    TokenDissociateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getTokenDissociate().toBuilder();;

        if (builder.hasAccount()) {
            accountId = AccountId.fromProtobuf(builder.getAccount());
        }

        for (var token : builder.getTokensList()) {
            tokenIds.add(TokenId.fromProtobuf(token));
        }
    }

    TokenDissociateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getTokenDissociate().toBuilder();;

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

    public TokenDissociateTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    public List<TokenId> getTokenIds() {
        return tokenIds;
    }

    public TokenDissociateTransaction setTokenIds(List<TokenId> tokens) {
        requireNotFrozen();
        this.tokenIds = tokens;
        return this;
    }

    TokenDissociateTransactionBody.Builder build() {
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
    void validateNetworkOnIds(@Nullable NetworkName networkName) {
        EntityIdHelper.validateNetworkOnIds(this.accountId, networkName);

        for (var token : tokenIds) {
            if (token != null) {
                EntityIdHelper.validateNetworkOnIds(token, networkName);
            }
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getDissociateTokensMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenDissociate(build());
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenDissociate(build());
    }
}
