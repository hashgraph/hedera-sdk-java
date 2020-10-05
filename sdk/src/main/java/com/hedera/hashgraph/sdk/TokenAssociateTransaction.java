package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenAssociateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hederahashgraph.service.proto.java.CryptoServiceGrpc;
import com.hederahashgraph.service.proto.java.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

public class TokenAssociateTransaction extends Transaction<TokenAssociateTransaction> {
    private final TokenAssociateTransactionBody.Builder builder;

    public TokenAssociateTransaction() {
        builder = TokenAssociateTransactionBody.newBuilder();
    }

    TokenAssociateTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenAssociate().toBuilder();
    }

    public AccountId getAccount() {
        return AccountId.fromProtobuf(builder.getAccount());
    }

    public TokenAssociateTransaction setAccount(AccountId accountId) {
        requireNotFrozen();
        builder.setAccount(accountId.toProtobuf());
        return this;
    }

    // Repeatable TokenID?

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getAssociateTokensMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenAssociate(builder);
        return true;
    }
}
