package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenGrantKycTransactionBody;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

public class TokenGrantKycTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenGrantKycTransaction> {
    private final TokenGrantKycTransactionBody.Builder builder;

    public TokenGrantKycTransaction() {
        builder = TokenGrantKycTransactionBody.newBuilder();
    }

    TokenGrantKycTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenGrantKyc().toBuilder();
    }

    public TokenId getToken() {
        return TokenId.fromProtobuf(builder.getToken());
    }

    public TokenGrantKycTransaction setToken(TokenId tokenId) {
        requireNotFrozen();
        builder.setToken(tokenId.toProtobuf());
        return this;
    }

    public AccountId getAccount() {
        return AccountId.fromProtobuf(builder.getAccount());
    }

    public TokenGrantKycTransaction setAccount(AccountId accountId) {
        requireNotFrozen();
        builder.setAccount(accountId.toProtobuf());
        return this;
    }

    @Override
    MethodDescriptor<Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getGrantKycToTokenAccountMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenGrantKyc(builder);
        return true;
    }
}
