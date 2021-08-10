package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.TokenAssociateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.annotation.Nullable;
import java.util.Objects;

public class TokenAssociateTransaction extends Transaction<TokenAssociateTransaction> {
    @Nullable
    private AccountId accountId = null;
    private List<TokenId> tokenIds = new ArrayList<>();

    public TokenAssociateTransaction() {
        setMaxTransactionFee(new Hbar(5));
    }

    TokenAssociateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TokenAssociateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    public TokenAssociateTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    public List<TokenId> getTokenIds() {
        return new ArrayList<>(tokenIds);
    }

    public TokenAssociateTransaction setTokenIds(List<TokenId> tokens) {
        Objects.requireNonNull(tokens);
        requireNotFrozen();
        this.tokenIds = new ArrayList<>(tokens);
        return this;
    }

    TokenAssociateTransactionBody.Builder build() {
        var builder = TokenAssociateTransactionBody.newBuilder();
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

    void initFromTransactionBody() {
        var body = txBody.getTokenAssociate();
        if (body.hasAccount()) {
            accountId = AccountId.fromProtobuf(body.getAccount());
        }

        for (var token : body.getTokensList()) {
            tokenIds.add(TokenId.fromProtobuf(token));
        }
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        Objects.requireNonNull(client);
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
        return TokenServiceGrpc.getAssociateTokensMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenAssociate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenAssociate(build());
    }
}
