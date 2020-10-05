package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenDissociateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hederahashgraph.service.proto.java.CryptoServiceGrpc;
import com.hederahashgraph.service.proto.java.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

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
