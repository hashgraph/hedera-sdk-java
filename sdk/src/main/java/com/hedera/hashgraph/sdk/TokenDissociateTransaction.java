package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.List;

public class TokenDissociateTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenDissociateTransaction> {
    private final TokenDissociateTransactionBody.Builder builder;

    public TokenDissociateTransaction() {
        builder = TokenDissociateTransactionBody.newBuilder();
    }

    TokenDissociateTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenDissociate().toBuilder();
    }

    public AccountId getAccount() {
        return AccountId.fromProtobuf(builder.getAccount());
    }

    public TokenDissociateTransaction setAccount(AccountId accountId) {
        requireNotFrozen();
        builder.setAccount(accountId.toProtobuf());
        return this;
    }

    public List<TokenID> getTokens() {
        return builder.getTokensList();
    }

    public TokenDissociateTransaction addTokens(TokenId... tokens) {
        requireNotFrozen();
        for (TokenId token : tokens) {
            builder.addTokens(token.toProtobuf());
        }
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getDissociateTokensMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenDissociate(builder);
        return true;
    }
}
