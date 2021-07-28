package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenDissociateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class TokenDissociateTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenDissociateTransaction> {
    private final TokenDissociateTransactionBody.Builder builder;

    @Nullable
    AccountId accountId = null;
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

    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    public TokenDissociateTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
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
    void validateChecksums(Client client) throws InvalidChecksumException {
        if (accountId != null) {
            accountId.validateChecksum(client);
        }

        for (var token : tokenIds) {
            if (token != null) {
                token.validateChecksum(client);
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
